package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree.Rect;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/*package*/ class Block {
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
    private final double historicalDataTime;
    private final double predictedTime;

    public Block(Rect bounds, Network network, int id, double historicalDataTime, double predictedTime) {
        this.bounds = bounds;
        this.id = id;
        centerCoord = new Coord(bounds.centerX, bounds.centerY);
        centerLink = NetworkUtils.getNearestLink(network, centerCoord);
        this.historicalDataTime = historicalDataTime;
        this.predictedTime = predictedTime;
    }

    public void addRoboTaxi(RoboTaxi roboTaxi) {
        GlobalAssert.that(contains(roboTaxi.getDivertableLocation().getCoord()));
        freeRoboTaxis.add(roboTaxi);
        freeRobotaxiInRebalancing = freeRoboTaxis.size();
    }

    /** REBALANCING CALCULATION FUNCTIONS */
    public void pushRobotaxiTo(Block block) {
        pushFromBlockToBlock(this, block);
    }
    
    /* package */ static void pushFromBlockToBlock(Block blockFrom, Block blockTo) {
        GlobalAssert.that(blockFrom.getAdjacentBlocks().contains(blockTo));
        GlobalAssert.that(blockTo.getBlockBalance() < blockFrom.getBlockBalance() - 1);
        GlobalAssert.that(blockFrom.freeRobotaxiInRebalancing > 0);
        GlobalAssert.that(blockFrom.hasAvailableRobotaxisToRebalance());
        blockFrom.adjacentBlocks.get(blockTo).incrementAndGet();
        blockFrom.freeRobotaxiInRebalancing -= 1;
        blockTo.freeRobotaxiInRebalancing += 1;
        blockFrom.calculateBlockBalanceInternal();
        blockTo.calculateBlockBalanceInternal();
    }

    public Block getAdjacentBlockWithLowestBalance() {
        return BlockUtils.getBlockwithLowestBalance(getAdjacentBlocks());
    }

    public Block getAdjacentBlockWithHighestBalanceAndAvailableRobotaxi() {
        return BlockUtils.getBlockwithHighestBalanceAndAvailableRobotaxi(getAdjacentBlocks());
    }

    public boolean lowerBalancesPresentInNeighbourhood() {
        return (getAdjacentBlockWithLowestBalance().blockBalance < this.blockBalance - 1);
    }

    public boolean higherBalancesPresentInNeighbourhood() {
        Block block = getAdjacentBlockWithHighestBalanceAndAvailableRobotaxi();
        if (Objects.isNull(block)) {
            return false;
        }
        return (getAdjacentBlockWithHighestBalanceAndAvailableRobotaxi().blockBalance > this.blockBalance + 1);
    }

    /** This function iterates over the four adjacent Blocks and sends to each the closest n vehicles which are needed
     * By Definition we have to push if the Integer Value in the adjacent Block Map is positiv
     * 
     * @param network
     * @param avRouter
     * @param now
     * @return */
    public Map<RoboTaxi, Block> executeRebalance(Network network, TravelTimeCalculatorCached timeDb, double now) {
        Map<RoboTaxi, Block> rebalanceDirectives = new HashMap<>();
        int numRebalancings = getNumberPushingVehicles();
        GlobalAssert.that(numRebalancings <= freeRoboTaxis.size());
        if (numRebalancings > 0) {
            GlobalAssert.that(!freeRoboTaxis.isEmpty());

            Set<Block> blocks = adjacentBlocks.keySet().stream().filter(b -> adjacentBlocks.get(b).intValue() > 0).collect(Collectors.toSet());
            NavigableMap<Double, Map<Block, Set<RoboTaxi>>> travelTimesSorted = new TreeMap<>();
            Map<Block, Set<Double>> blocktravelTimes = new HashMap<>();
            blocks.forEach(b -> blocktravelTimes.put(b, new HashSet<>()));

            Map<RoboTaxi, Map<Block, Double>> allTravelTimesForRoboTaxis = new HashMap<>();
            freeRoboTaxis.forEach(rt -> allTravelTimesForRoboTaxis.put(rt, new HashMap<>()));

            for (RoboTaxi roboTaxi : freeRoboTaxis) {
                for (Block block : blocks) {
                    double travelTime = timeDb.timeFromTo(roboTaxi.getDivertableLocation(), block.centerLink).number().doubleValue();
                    if (!travelTimesSorted.containsKey(travelTime)) {
                        travelTimesSorted.put(travelTime, new HashMap<>());
                    }
                    if (!travelTimesSorted.get(travelTime).containsKey(block)) {
                        travelTimesSorted.get(travelTime).put(block, new HashSet<>());
                    }
                    travelTimesSorted.get(travelTime).get(block).add(roboTaxi);
                    blocktravelTimes.get(block).add(travelTime);
                    allTravelTimesForRoboTaxis.get(roboTaxi).put(block, travelTime);
                }
            }

            Map<Block, Set<RoboTaxi>> addedRoboTaxisPerBlock = new HashMap<>();
            blocks.forEach(b -> addedRoboTaxisPerBlock.put(b, new HashSet<>()));

            for (int i = 0; i < numRebalancings; i++) {

                GlobalAssert.that(!travelTimesSorted.isEmpty());
                Entry<Double, Map<Block, Set<RoboTaxi>>> nearestTrips = travelTimesSorted.firstEntry();
                Double travelTime = nearestTrips.getKey();
                Block block = nearestTrips.getValue().keySet().iterator().next();
                RoboTaxi roboTaxi = nearestTrips.getValue().get(block).iterator().next();

                rebalanceDirectives.put(roboTaxi, block);
                addedRoboTaxisPerBlock.get(block).add(roboTaxi);
                freeRoboTaxis.remove(roboTaxi);

                // remove All The entries where the just added RoboTaxi Occured
                for (Entry<Block, Double> entry : allTravelTimesForRoboTaxis.get(roboTaxi).entrySet()) {
                    BlockUtils.removeRoboTaxiFromMap(travelTimesSorted, entry.getValue(), entry.getKey(), roboTaxi);
                }

                // If the adjacent block has received all the required Taxis, remove it from all travel times
                int updatedPushing = adjacentBlocks.get(block).decrementAndGet();
                if (updatedPushing == 0) {
                    for (double travelTimeBlock : blocktravelTimes.get(block)) {
                        if (travelTimeBlock >= travelTime) {
                            BlockUtils.removeBlockFromMap(travelTimesSorted, travelTimeBlock, block);
                        }
                    }
                }
            }
        }

        adjacentBlocks.forEach((b, ai) -> GlobalAssert.that(ai.intValue() == 0));
        GlobalAssert.that(rebalanceDirectives.size() == numRebalancings);

        return rebalanceDirectives;
    }



    public Link getCenterLink() {
        return centerLink;
    }

    public boolean hasAvailableRobotaxisToRebalance() {
        return freeRoboTaxis.size() > getNumberPushingVehicles();
    }

    private int getNumberPushingVehicles() {
        return adjacentBlocks.values().stream().mapToInt(aI -> aI.intValue()).filter(aI -> aI > 0).sum();
    }

    public boolean contains(Coord coord) {
        return bounds.contains(coord.getX(), coord.getY());
    }

    public void removeAllRobotaxis() {
        freeRoboTaxis.clear();
    }

    public void addUnassignedRequest() {
        numberUnassignedRequests++;
    }

    public void addRequestLastHour() {
        numberRequestsHistorical += 1;
    }

    public void removeAllRequestsLastHour() {
        numberRequestsHistorical = 0;
    }

    public void removeAllUnassignedRequests() {
        numberUnassignedRequests = 0;
    }

    public void addAdjacentBlock(Block block) {
        adjacentBlocks.put(block, new AtomicInteger(0));
    }

    public Set<Block> getAdjacentBlocks() {
        return adjacentBlocks.keySet();
    }


    public int getNumberOfUnassignedRequests() {
        return numberUnassignedRequests;
    }

    public int getNumberOfExpectedRequests() {
        return (int) Math.round(numberRequestsHistorical / historicalDataTime * predictedTime);
    }
    

    public void calculateBlockBalance(int savTotal, int demandTotal) {
        scenarioFreeRoboTaxis = savTotal;
        scenarioUnassignedRequests = demandTotal;
        calculateBlockBalanceInternal();
    }

    private void calculateBlockBalanceInternal() {
        blockBalance = Math.round(BlockUtils.calculateBlockBalance(scenarioFreeRoboTaxis, freeRobotaxiInRebalancing, scenarioUnassignedRequests,
                getNumberOfUnassignedRequests() + getNumberOfExpectedRequests()));
    }

    public long getBlockBalance() {
        return blockBalance;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rect) {
            Rect rect = (Rect) obj;
            return (rect.minX == this.bounds.minX && //
                    rect.minY == this.bounds.minY && //
                    rect.maxX == this.bounds.maxX && //
                    rect.maxY == this.bounds.maxY);
        }
        return super.equals(obj);
    }


}
