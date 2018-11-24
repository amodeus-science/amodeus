package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ class BlockRebalancingHelper {

    private final NavigableMap<Double, Map<Block, Set<RoboTaxi>>> travelTimesSorted = new TreeMap<>();
    private final Map<Block, Set<Double>> blocktravelTimes = new HashMap<>();
    private final Map<RoboTaxi, Map<Block, Double>> allTravelTimesForRoboTaxis = new HashMap<>();

    /* package */ BlockRebalancingHelper(Set<Block> blocks, Set<RoboTaxi> freeRoboTaxis, TravelTimeCalculatorCached timeDb) {
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

    /* package */ void update(ShortestTrip shortestTrip, int updatedPushing) {
        // remove All The entries where the just added RoboTaxi Occured
        for (Entry<Block, Double> entry : allTravelTimesForRoboTaxis.get(shortestTrip.roboTaxi).entrySet()) {
            removeRoboTaxiFromMap(travelTimesSorted, entry.getValue(), entry.getKey(), shortestTrip.roboTaxi);
        }

        // If the adjacent block has received all the required Taxis, remove it from all travel times
        if (updatedPushing == 0) {
            for (double travelTimeBlock : blocktravelTimes.get(shortestTrip.block)) {
                if (travelTimeBlock >= shortestTrip.travelTime) {
                    removeBlockFromMap(travelTimesSorted, travelTimeBlock, shortestTrip.block);
                }
            }
        }
    }

    /* package */ ShortestTrip getShortestTrip() {
        GlobalAssert.that(!travelTimesSorted.isEmpty());
        return new ShortestTrip();
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

    /* package */ class ShortestTrip {
        /* package */ final Double travelTime;
        /* package */ final Block block;
        /* package */ final RoboTaxi roboTaxi;

        /* package */ ShortestTrip() {
            Entry<Double, Map<Block, Set<RoboTaxi>>> nearestTrips = travelTimesSorted.firstEntry();
            this.travelTime = nearestTrips.getKey();
            this.block = nearestTrips.getValue().keySet().iterator().next();
            this.roboTaxi = nearestTrips.getValue().get(block).iterator().next();
        }
    }
}
