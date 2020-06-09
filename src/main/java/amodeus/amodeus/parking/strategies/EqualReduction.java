/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.parking.strategies;

import java.util.Map;

import org.matsim.api.core.v01.network.Link;

public enum EqualReduction {
    ;

    public static void apply(Map<Link, Integer> unitsToMove, int totalSpots) {
        int totalUnits = unitsToMove.values().stream().mapToInt(i -> i).sum();
        int diff = totalUnits - totalSpots;
        int locations = unitsToMove.size();
        int remove = (int) Math.ceil(((double) diff) / locations);
        unitsToMove.replaceAll((link, i) -> Math.max(i - remove, 0));
    }
}
