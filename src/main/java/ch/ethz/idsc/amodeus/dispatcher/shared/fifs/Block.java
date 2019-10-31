/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree.Rect;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.routing.NetworkTimeDistInterface;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/** A {@link Block} is the central Element for the Block Rebalancing- It is a virtual rectangular zone placed in a grid. Thus it has four adjacent blocks.
 * Within a Block there are free RoboTaxis, open Requests and historical Requests. This is the amount of requests in a predefined time duration in the past.
 * Each Block can push robo Taxis to its adjacent Blocks or receive roboTaxis from them. First it is assigned how many Robotaxis should be pushed and then in a
 * second step it is calculated which is the best assignment of robotaxi to rebalance link. */
/* package */ class Block {
    /** block ID */
    private final int id;

    /** geometrical properties */
    private final Rect bounds;
    private final Link centerLink;

    /** All the adjacent Blocks with an Integer for the number of Planed
     * RebalancingVehicles negative if sending and positive if receiving. */
    private final Map<Block, AtomicInteger> adjacentBlocks = new HashMap<>();

    /** RoboTaxis and Requests in the Block */
    private final Set<RoboTaxi> freeRoboTaxis = new HashSet<>();
    /** Properties of the Rebalancing */
    private final double predictionFraction;
    // ---
    private int freeRobotaxiInRebalancing;
    private int numberRequestsHistorical = 0;
    private int numberUnassignedRequests = 0;
    private long blockBalance;

    /** Total Properties in Scenario */
    private int scenarioFreeRoboTaxis;
    private int scenarioUnassignedRequests;

    // *******************************************************************/
    // * INITIALISATION FUNCTIONS only called once */
    // *******************************************************************/
    /** generates a new {@link Block}.
     * 
     * @param bounds defines the bounds of the Block
     * @param network is used to find the center LInk
     * @param id is the identifier for this Block
     * @param historicalDataTime Time over how long requests are stored. Is used for balance calculation
     * @param predictedTime what is the time for which the future requests are predicted. Normally this value should be in the order of the dispatch period */
    Block(Rect bounds, Network network, int id, double historicalDataTime, double predictedTime) {
        this.bounds = bounds;
        this.id = id;
        Coord centerCoord = new Coord(bounds.centerX, bounds.centerY);
        centerLink = NetworkUtils.getNearestLink(network, centerCoord);
        this.predictionFraction = predictedTime / historicalDataTime;
    }

    /** adds a Block as an adjacent block to this block. This function should only be called if the Block grid is set up.
     * 
     * @param block new adjacent {@link Block} */
    void addAdjacentBlock(Block block) {
        adjacentBlocks.put(block, new AtomicInteger(0));
    }

    // *******************************************************************/
    // * UPDATE FUNCTIONS called once in each time step before push pull */
    // *******************************************************************/

    /** clears all values for the current robotaxis and sets the current number of unassigned and historical Requests to zero. */
    void clear() {
        freeRoboTaxis.clear();
        numberRequestsHistorical = 0;
        numberUnassignedRequests = 0;
    }

    /** adds a Robotaxi to the free robotaxi set in this block
     * 
     * @param roboTaxi */
    void addRoboTaxi(RoboTaxi roboTaxi) {
        GlobalAssert.that(contains(roboTaxi.getDivertableLocation().getCoord()));
        freeRoboTaxis.add(roboTaxi);
        freeRobotaxiInRebalancing = freeRoboTaxis.size();
    }

    /** increases the number of unassigned Requests by one */
    void addUnassignedRequest() {
        ++numberUnassignedRequests;
    }

    /** increases the number of historical Requests in this block by one. checks if the given link is in the block.
     * 
     * @param link */
    void addRequestLastHour(Link link) {
        GlobalAssert.that(contains(link.getCoord()));
        numberRequestsHistorical += 1;
    }

    // *******************************************************************/
    // * PUSH PULL FUNCTIONS to execute rebalancing based on the balance */
    // *******************************************************************/

    /** calculates the initial Block balances for based on the total free Robotaxis and the total demand in the scenario for a given timestep.
     * 
     * @param savTotal number of free roboTaxis in the Scenario
     * @param demandTotal number of unassigned Requests in the scenario */
    void calculateInitialBlockBalance(int savTotal, int demandTotal) {
        scenarioFreeRoboTaxis = savTotal;
        scenarioUnassignedRequests = demandTotal;
        calculateBlockBalanceInternal();
    }

    /** updates the block balance internally */
    private void calculateBlockBalanceInternal() {
        blockBalance = Math.round(BlockUtils.calculateBlockBalance(scenarioFreeRoboTaxis, freeRobotaxiInRebalancing, scenarioUnassignedRequests,
                numberUnassignedRequests + (int) Math.round(numberRequestsHistorical * predictionFraction)));
    }

    /** Plans to push a robotaxi from this block into the given Block.
     * Throws Exception if:
     * - the Block is not a adjacent block.
     * - the two blocks have not at least a difference in the block balance of 1
     * - this block has no free robotaxis to rebalance
     * - this block plans already to move all available robotaxis
     * 
     * After the call of this function both block balances are updated.
     * 
     * @param block */
    void pushRobotaxiTo(Block block) {
        GlobalAssert.that(this.getAdjacentBlocks().contains(block));
        GlobalAssert.that(block.getBlockBalance() < this.getBlockBalance() - 1);
        GlobalAssert.that(this.freeRobotaxiInRebalancing > 0);
        GlobalAssert.that(this.hasAvailableRobotaxisToRebalance());
        this.adjacentBlocks.get(block).incrementAndGet();
        this.freeRobotaxiInRebalancing -= 1;
        block.freeRobotaxiInRebalancing += 1;
        this.calculateBlockBalanceInternal();
        block.calculateBlockBalanceInternal();
    }

    /** This function gives back the rebalance directives based on all the planed Movements of Robotaxis which were done with the {@link pushRobotaxiTo()}
     * method. */
    RebalancingDirectives executeRebalance(NetworkTimeDistInterface timeDb, double now) {
        Map<RoboTaxi, Link> rebalanceDirectives = new HashMap<>();
        /** calculate the number of pushes from this block */
        int numRebalancings = getNumberPushingVehicles();
        GlobalAssert.that(numRebalancings <= freeRoboTaxis.size());

        if (numRebalancings > 0) {
            Set<Block> blocks = adjacentBlocks.keySet().stream().filter(b -> adjacentBlocks.get(b).intValue() > 0).collect(Collectors.toSet());
            BlockRebalancingHelper blockHelper = new BlockRebalancingHelper(blocks, freeRoboTaxis, timeDb, now);
            /** for all planed pushes */
            for (int i = 0; i < numRebalancings; i++) {
                /** find the shortest possible trip for all Robotaxis and blocks which need roboTaxis from this blcok */
                ShortestTrip shortestTrip = blockHelper.getShortestTrip();

                rebalanceDirectives.put(shortestTrip.roboTaxi, shortestTrip.block.centerLink);
                freeRoboTaxis.remove(shortestTrip.roboTaxi);
                int updatedPushing = adjacentBlocks.get(shortestTrip.block).decrementAndGet();

                blockHelper.update(shortestTrip, updatedPushing);
            }
        }

        adjacentBlocks.values().forEach(ai -> GlobalAssert.that(ai.intValue() == 0));
        GlobalAssert.that(rebalanceDirectives.size() == numRebalancings);

        return new RebalancingDirectives(rebalanceDirectives);
    }

    // *******************************************************************/
    // * HELPER Functions */
    // *******************************************************************/

    /** checks if the Block has more Robotaxis which can be rebalanced
     * 
     * @return */
    boolean hasAvailableRobotaxisToRebalance() {
        return freeRoboTaxis.size() > getNumberPushingVehicles();
    }

    /** checks if a coordinate lies in this Block
     * 
     * @param coord
     * @return */
    boolean contains(Coord coord) {
        return bounds.contains(coord.getX(), coord.getY());
    }

    /** calcualtes the number of planned pushes to the adjacent blocks */
    private int getNumberPushingVehicles() {
        return adjacentBlocks.values().stream().mapToInt(AtomicInteger::intValue).filter(aI -> aI > 0).sum();
    }

    // *******************************************************************/
    // * GETTERS and SETTERS */
    // *******************************************************************/

    /** @return the closest Link in the Network to the center coordinate */
    Link getCenterLink() {
        return centerLink;
    }

    /** @return a Set of all the adjacent Blocks */
    Set<Block> getAdjacentBlocks() {
        return adjacentBlocks.keySet();
    }

    /** @return the current Block balance */
    long getBlockBalance() {
        return blockBalance;
    }

    /** @return the identifier defined in the constructor of this Block */
    int getId() {
        return id;
    }
}
