/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.network.Link;

public enum EqualReduction {
    ;

    public static void apply(Map<Link, Integer> unitsToMove, int totalSpots) {
        int totalUnits = unitsToMove.values().stream().mapToInt(i -> i).sum();
        int diff = totalUnits - totalSpots;
        int locations = unitsToMove.size();
        int remove = (int) Math.ceil(((double) diff) / locations);
        for (Entry<Link, Integer> entry : unitsToMove.entrySet()) {
            unitsToMove.put(entry.getKey(), Math.max(entry.getValue() - remove, 0));
        }
    }
}
