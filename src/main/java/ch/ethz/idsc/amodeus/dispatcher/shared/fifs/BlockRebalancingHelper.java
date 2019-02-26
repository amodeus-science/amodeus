/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.routing.NetworkTimeDistInterface;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

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
    BlockRebalancingHelper(Set<Block> blocks, Set<RoboTaxi> freeRoboTaxis, NetworkTimeDistInterface timeDb) {
        GlobalAssert.that(!freeRoboTaxis.isEmpty());

        blocks.forEach(b -> blocktravelTimes.put(b, new HashSet<>()));
        freeRoboTaxis.forEach(rt -> allTravelTimesForRoboTaxis.put(rt, new HashMap<>()));
        for (RoboTaxi roboTaxi : freeRoboTaxis) {
            for (Block block : blocks) {
                double travelTime = timeDb.timeFromTo(roboTaxi.getDivertableLocation(), block.getCenterLink()).number().doubleValue();
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
    }

    /** remove the shortest trip and all the trips which are associated with this trip.
     * 
     * @param shortestTrip shortest Trip which was used outside
     * @param updatedPushing value of how many pushes are still required to the block in shortest trip. */
    void update(ShortestTrip shortestTrip, int updatedPushing) {
        // remove All The entries where the just added RoboTaxi Occured
        for (Entry<Block, Double> entry : allTravelTimesForRoboTaxis.get(shortestTrip.roboTaxi).entrySet()) {
            removeRoboTaxiFromMap(entry.getValue(), entry.getKey(), shortestTrip.roboTaxi);
        }

        // If the adjacent block has received all the required Taxis, remove it from all travel times
        if (updatedPushing == 0) {
            for (double travelTimeBlock : blocktravelTimes.get(shortestTrip.block)) {
                if (travelTimeBlock >= shortestTrip.travelTime) {
                    removeBlockFromMap(travelTimeBlock, shortestTrip.block);
                }
            }
        }
    }

    private void removeRoboTaxiFromMap(double travelTime, Block block, RoboTaxi roboTaxi) {
        if (travelTimesSorted.containsKey(travelTime)) {
            if (travelTimesSorted.get(travelTime).containsKey(block)) {
                if (travelTimesSorted.get(travelTime).get(block).contains(roboTaxi)) {
                    travelTimesSorted.get(travelTime).get(block).remove(roboTaxi);
                    if (travelTimesSorted.get(travelTime).get(block).isEmpty()) {
                        removeBlockFromMap(travelTime, block);
                    }
                }
            }
        }
    }

    private void removeBlockFromMap(double travelTime, Block block) {
        if (travelTimesSorted.containsKey(travelTime)) {
            if (travelTimesSorted.get(travelTime).containsKey(block)) {
                travelTimesSorted.get(travelTime).remove(block);
                if (travelTimesSorted.get(travelTime).isEmpty()) {
                    travelTimesSorted.remove(travelTime);
                }
            }
        }
    }

    /** gets the currently shortest trip in the data structure. If some trips have equal length a random choice is made.
     * 
     * @return */
    ShortestTrip getShortestTrip() {
        GlobalAssert.that(!travelTimesSorted.isEmpty());
        return new ShortestTrip();
    }

    /** Helper class to wrap the three elements Travel Time, Block and roboTaxi */
    class ShortestTrip {
        final Double travelTime;
        final Block block;
        final RoboTaxi roboTaxi;

        ShortestTrip() {
            Entry<Double, Map<Block, Set<RoboTaxi>>> nearestTrips = travelTimesSorted.firstEntry();
            this.travelTime = nearestTrips.getKey();
            this.block = nearestTrips.getValue().keySet().iterator().next();
            this.roboTaxi = nearestTrips.getValue().get(block).iterator().next();
        }
    }
}
