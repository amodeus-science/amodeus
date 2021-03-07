/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.fifs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.routing.NetworkTimeDistInterface;
import amodeus.amodeus.util.math.GlobalAssert;
import amodeus.amodeus.util.math.Scalar2Number;

/** helper Class for a Block to translate planned pushes and pulls into directives of Robotaxis to Links.
 * calculates all travel times of all robotaxis to all the blocks to which pushes and pulls are planned. */
/* package */ class BlockRebalancingHelper {

    private final NavigableMap<Double, Map<Block, Set<RoboTaxi>>> travelTimesSorted = new TreeMap<>();
    private final Map<Block, Set<Double>> blocktravelTimes = new HashMap<>();
    private final Map<RoboTaxi, Map<Block, Double>> allTravelTimesForRoboTaxis = new HashMap<>();

    /** Initialisation of the helper class.
     * calculation of all travel times of robotaxis to the center of the given Blocks.
     * 
     * @param blocks all the adjacent blocks to which pushes are planned
     * @param freeRoboTaxis all the robotaxis which should be considered
     * @param timeDb Travel time calculator */
    public BlockRebalancingHelper(Set<Block> blocks, Set<RoboTaxi> freeRoboTaxis, NetworkTimeDistInterface timeDb, double now) {
        GlobalAssert.that(!freeRoboTaxis.isEmpty());

        blocks.forEach(b -> blocktravelTimes.put(b, new HashSet<>()));
        freeRoboTaxis.forEach(rt -> allTravelTimesForRoboTaxis.put(rt, new HashMap<>()));
        for (RoboTaxi roboTaxi : freeRoboTaxis)
            for (Block block : blocks) {
                double travelTime = Scalar2Number.of(timeDb.travelTime(roboTaxi.getDivertableLocation(), block.getCenterLink(), now)).doubleValue();

                travelTimesSorted.computeIfAbsent(travelTime, t -> new HashMap<>()) //
                        /* travelTimesSorted.get(travelTime) */ .computeIfAbsent(block, b -> new HashSet<>()) //
                        /* travelTimesSorted.get(travelTime).get(block) */ .add(roboTaxi);
                blocktravelTimes.get(block).add(travelTime);
                allTravelTimesForRoboTaxis.get(roboTaxi).put(block, travelTime);
            }
    }

    /** remove the shortest trip and all the trips which are associated with this trip.
     * 
     * @param shortestTrip shortest Trip which was used outside
     * @param updatedPushing value of how many pushes are still required to the block in shortest trip. */
    public void update(ShortestTrip shortestTrip, int updatedPushing) {
        // remove All The entries where the just added RoboTaxi Occured
        allTravelTimesForRoboTaxis.get(shortestTrip.roboTaxi).forEach((block, travelTime) -> removeRoboTaxiFromMap(travelTime, block, shortestTrip.roboTaxi));

        // If the adjacent block has received all the required Taxis, remove it from all travel times
        if (updatedPushing == 0)
            for (double travelTimeBlock : blocktravelTimes.get(shortestTrip.block))
                if (travelTimeBlock >= shortestTrip.travelTime)
                    removeBlockFromMap(travelTimeBlock, shortestTrip.block);
    }

    private void removeRoboTaxiFromMap(double travelTime, Block block, RoboTaxi roboTaxi) {
        if (travelTimesSorted.containsKey(travelTime))
            if (travelTimesSorted.get(travelTime).containsKey(block))
                if (travelTimesSorted.get(travelTime).get(block).contains(roboTaxi)) {
                    travelTimesSorted.get(travelTime).get(block).remove(roboTaxi);
                    if (travelTimesSorted.get(travelTime).get(block).isEmpty())
                        removeBlockFromMap(travelTime, block);
                }
    }

    private void removeBlockFromMap(double travelTime, Block block) {
        if (travelTimesSorted.containsKey(travelTime))
            if (travelTimesSorted.get(travelTime).containsKey(block)) {
                travelTimesSorted.get(travelTime).remove(block);
                if (travelTimesSorted.get(travelTime).isEmpty())
                    travelTimesSorted.remove(travelTime);
            }
    }

    /** gets the currently shortest trip in the data structure. If some trips have equal length a random choice is made.
     * 
     * @return */
    public ShortestTrip getShortestTrip() {
        GlobalAssert.that(!travelTimesSorted.isEmpty());
        return new ShortestTrip(travelTimesSorted.firstEntry());
    }
}
