package ch.ethz.idsc.amodeus.dispatcher.shared.kockelman;

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

    /** Requests and Taxis in Block */
    private int unassignedRequests;
    private final Set<RoboTaxi> freeRoboTaxis;
    private int freeRobotaxiInRebalancing;
    private final Set<Coord> allRequestCoordsLastHour = new HashSet<>();
    private long blockBalance;

    /** Total Properties in Scenario */
    private int totalFreeRoboTaxis;
    private int totalUnassignedRequests;

    /** Properties of the Rebalancing */
    // TODO should come from OUtside
    private static final double TIMEOFAVERAGE = 3600.0;
    private static final double PERDICTEDTIME = 300.0;

    public Block(Rect bounds, Network network, int id) {
        this.bounds = bounds;

        this.id = id;
        centerCoord = new Coord(bounds.centerX, bounds.centerY);
        centerLink = NetworkUtils.getNearestLink(network, centerCoord);
        unassignedRequests = 0;
        freeRoboTaxis = new HashSet<>();
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

    private static void pushFromBlockToBlock(Block blockFrom, Block blockTo) {
        GlobalAssert.that(blockFrom.adjacentBlocks.containsKey(blockTo));
        GlobalAssert.that(blockTo.getBlockBalance() < blockFrom.getBlockBalance() - 1);
        GlobalAssert.that(blockFrom.freeRobotaxiInRebalancing > 0);
        GlobalAssert.that(blockFrom.hasAvailableRobotaxisToRebalance());
        blockFrom.adjacentBlocks.get(blockTo).incrementAndGet();
        blockFrom.freeRobotaxiInRebalancing -= 1;
        blockTo.freeRobotaxiInRebalancing += 1;
        // TODO Check if its valid to update both block balances
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
    public Map<RoboTaxi, Block> executeRebalance(Network network, LeastCostCalculatorDatabaseOneTime timeDb, double now) {
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
                    removeRoboTaxiFromMap(travelTimesSorted, entry.getValue(), entry.getKey(), roboTaxi);
                }

                // If the adjacent block has received all the required Taxis, remove it from all travel times
                int updatedPushing = adjacentBlocks.get(block).decrementAndGet();
                if (updatedPushing == 0) {
                    for (double travelTimeBlock : blocktravelTimes.get(block)) {
                        if (travelTimeBlock >= travelTime) {
                            removeBlockFromMap(travelTimesSorted, travelTimeBlock, block);
                        }
                    }
                }
            }
        }

        adjacentBlocks.forEach((b, ai) -> GlobalAssert.that(ai.intValue() == 0));
        GlobalAssert.that(rebalanceDirectives.size() == numRebalancings);

        return rebalanceDirectives;
    }

    private static void removeRoboTaxiFromMap(NavigableMap<Double, Map<Block, Set<RoboTaxi>>> travelTimesSorted, double travelTime, Block block, RoboTaxi roboTaxi) {
        if (travelTimesSorted.containsKey(travelTime)) {
            if (travelTimesSorted.get(travelTime).containsKey(block)) {
                if (travelTimesSorted.get(travelTime).get(block).contains(roboTaxi)) {
                    travelTimesSorted.get(travelTime).get(block).remove(roboTaxi);
                    if (travelTimesSorted.get(travelTime).get(block).isEmpty()) {
                        removeBlockFromMap(travelTimesSorted, travelTime, block);
                    }
                }
            }
        }
    }

    private static void removeBlockFromMap(NavigableMap<Double, Map<Block, Set<RoboTaxi>>> travelTimesSorted, double travelTime, Block block) {
        if (travelTimesSorted.containsKey(travelTime)) {
            if (travelTimesSorted.get(travelTime).containsKey(block)) {
                travelTimesSorted.get(travelTime).remove(block);
                if (travelTimesSorted.get(travelTime).isEmpty()) {
                    travelTimesSorted.remove(travelTime);
                }
            }
        }
    }

    public Link getCenterLink() {
        return centerLink;
    }

    /** Normal getter and Setter fucntions */

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
        unassignedRequests++;
    }

    public void addRequestCoordLastHour(Coord requestCoordLastHour) {
        GlobalAssert.that(contains(requestCoordLastHour));
        allRequestCoordsLastHour.add(requestCoordLastHour);
    }

    public void removeAllRequestCoordsLastHour() {
        allRequestCoordsLastHour.clear();
    }

    public void removeAllUnassignedRequests() {
        unassignedRequests = 0;
    }

    public void addAdjacentBlock(Block block) {
        adjacentBlocks.put(block, new AtomicInteger(0));
    }

    public Set<Block> getAdjacentBlocks() {
        return adjacentBlocks.keySet();
    }

    public void removeAdjacentblock(Block block) {
        GlobalAssert.that(adjacentBlocks.containsKey(block));
        adjacentBlocks.remove(block);
    }

    public Rect getBounds() {
        return bounds;
    }

    public int getNumberOfUnassignedRequests() {
        return unassignedRequests;
    }

    public int getNumberOfExpectedRequests() {
        return (int) Math.round(allRequestCoordsLastHour.size() / TIMEOFAVERAGE * PERDICTEDTIME);
        // return expectedRequests;
    }

    public Set<RoboTaxi> getFreeRoboTaxis() {
        return freeRoboTaxis;
    }

    // public int getNumberOfFreeRoboTaxisReal() {
    // return freeRoboTaxis.size();
    // }

    public void calculateBlockBalance(int savTotal, int demandTotal) {
        totalFreeRoboTaxis = savTotal;
        totalUnassignedRequests = demandTotal;
        calculateBlockBalanceInternal();
    }

    private void calculateBlockBalanceInternal() {
        blockBalance = Math.round(BlockUtils.calculateBlockBalance(totalFreeRoboTaxis, freeRobotaxiInRebalancing, totalUnassignedRequests,
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

    @Override
    public int hashCode() {
        // double hash = 17;
        // hash = hash * 31 + this.bounds.minX;
        // hash = hash * 31 + this.bounds.maxX;
        // hash = hash * 31 + this.bounds.minY;
        // hash = hash * 31 + this.bounds.maxY;
        // return (int) hash;
        return super.hashCode();
    }

    /** @param roboTaxi
     * @return {@link Coord} with {@link RoboTaxi} location */
    /* package */ Coord getRoboTaxiLoc(RoboTaxi roboTaxi) {
        return roboTaxi.getDivertableLocation().getCoord();
    }

}
