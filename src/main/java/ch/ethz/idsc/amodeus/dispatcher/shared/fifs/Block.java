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
import ch.ethz.idsc.amodeus.dispatcher.shared.fifs.BlockRebalancingHelper.ShortestTrip;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ class Block {
    /** block ID */
    private final int id;

    /** geometrical Properties */
    private final Rect bounds;
    private final Coord centerCoord;
    private final Link centerLink;

    /** All the adjacent Blocks with an Interger for the number of Planed
     * RebalancingVehicles negative if sending and positiv if receiving. */
    private final Map<Block, AtomicInteger> adjacentBlocks = new HashMap<>();

    /** RoboTaxis and Requests in the Block */
    private final Set<RoboTaxi> freeRoboTaxis = new HashSet<>();
    private int freeRobotaxiInRebalancing;
    private int numberRequestsHistorical = 0;
    private int numberUnassignedRequests = 0;
    private long blockBalance;

    /** Total Properties in Scenario */
    private int scenarioFreeRoboTaxis;
    private int scenarioUnassignedRequests;

    /** Properties of the Rebalancing */
    private final double predictionFraction;

    // *******************************************************************/
    // * INITIALISATION FUNCTIONS only called once */
    // *******************************************************************/

    /* package */ Block(Rect bounds, Network network, int id, double historicalDataTime, double predictedTime) {
        this.bounds = bounds;
        this.id = id;
        centerCoord = new Coord(bounds.centerX, bounds.centerY);
        centerLink = NetworkUtils.getNearestLink(network, centerCoord);
        this.predictionFraction = predictedTime / historicalDataTime;
    }

    /* package */ void addAdjacentBlock(Block block) {
        adjacentBlocks.put(block, new AtomicInteger(0));
    }

    // *******************************************************************/
    // * UPDATE FUNCTIONS called once in each time step before push pull */
    // *******************************************************************/

    /* package */ void clear() {
        freeRoboTaxis.clear();
        numberRequestsHistorical = 0;
        numberUnassignedRequests = 0;
    }

    /* package */ void addRoboTaxi(RoboTaxi roboTaxi) {
        GlobalAssert.that(contains(roboTaxi.getDivertableLocation().getCoord()));
        freeRoboTaxis.add(roboTaxi);
        freeRobotaxiInRebalancing = freeRoboTaxis.size();
    }

    /* package */ void addUnassignedRequest() {
        numberUnassignedRequests++;
    }

    /* package */ void addRequestLastHour(Link link) {
        GlobalAssert.that(contains(link.getCoord()));
        numberRequestsHistorical += 1;
    }

    // *******************************************************************/
    // * PUSH PuLL FUNCTIONS to execute rebalancing based on the balance */
    // *******************************************************************/
    /* package */ void calculateInitialBlockBalance(int savTotal, int demandTotal) {
        scenarioFreeRoboTaxis = savTotal;
        scenarioUnassignedRequests = demandTotal;
        calculateBlockBalanceInternal();
    }

    private void calculateBlockBalanceInternal() {
        blockBalance = Math.round(BlockUtils.calculateBlockBalance(scenarioFreeRoboTaxis, freeRobotaxiInRebalancing, scenarioUnassignedRequests,
                numberUnassignedRequests + (int) Math.round(numberRequestsHistorical * predictionFraction)));
    }

    /* package */ void pushRobotaxiTo(Block block) {
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

    /** This function iterates over the four adjacent Blocks and sends to each the closest n vehicles which are needed
     * By Definition we have to push if the Integer Value in the adjacent Block Map is positiv */
    /* package */ Map<RoboTaxi, Block> executeRebalance(TravelTimeCalculator timeDb) {
        Map<RoboTaxi, Block> rebalanceDirectives = new HashMap<>();
        int numRebalancings = getNumberPushingVehicles();
        GlobalAssert.that(numRebalancings <= freeRoboTaxis.size());

        if (numRebalancings > 0) {
            Set<Block> blocks = adjacentBlocks.keySet().stream().filter(b -> adjacentBlocks.get(b).intValue() > 0).collect(Collectors.toSet());
            BlockRebalancingHelper blockHelper = new BlockRebalancingHelper(blocks, freeRoboTaxis, timeDb);

            for (int i = 0; i < numRebalancings; i++) {
                ShortestTrip shortestTrip = blockHelper.getShortestTrip();

                rebalanceDirectives.put(shortestTrip.roboTaxi, shortestTrip.block);
                freeRoboTaxis.remove(shortestTrip.roboTaxi);
                int updatedPushing = adjacentBlocks.get(shortestTrip.block).decrementAndGet();

                blockHelper.update(shortestTrip, updatedPushing);
            }
        }

        adjacentBlocks.values().forEach(ai -> GlobalAssert.that(ai.intValue() == 0));
        GlobalAssert.that(rebalanceDirectives.size() == numRebalancings);

        return rebalanceDirectives;
    }

    // *******************************************************************/
    // * HELPER Functions */
    // *******************************************************************/

    /* package */ boolean hasAvailableRobotaxisToRebalance() {
        return freeRoboTaxis.size() > getNumberPushingVehicles();
    }

    /* package */ boolean contains(Coord coord) {
        return bounds.contains(coord.getX(), coord.getY());
    }

    private int getNumberPushingVehicles() {
        return adjacentBlocks.values().stream().mapToInt(aI -> aI.intValue()).filter(aI -> aI > 0).sum();
    }

    // *******************************************************************/
    // * GETTERS and SETTERS */
    // *******************************************************************/

    /* package */ Link getCenterLink() {
        return centerLink;
    }

    /* package */ Set<Block> getAdjacentBlocks() {
        return adjacentBlocks.keySet();
    }

    /* package */ long getBlockBalance() {
        return blockBalance;
    }

    /* package */ int getId() {
        return id;
    }

}
