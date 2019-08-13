/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/* package */ enum StaticHelper {
    ;

    /** @return {@link Map} with {@link Link}s that {@link RoboTaxi}s are driving towards
     *         and for each {@link Link} the number of {@link RoboTaxi}s driving there based on a
     *         {@link Collection} of {@link RoboTaxi}s @param roboTaxis */
    public static Map<Link, Integer> getDestinationCount(Collection<RoboTaxi> roboTaxis) {
        Map<Link, Integer> destCount = new HashMap<>();
        roboTaxis.stream().map(rt -> rt.getCurrentDriveDestination())//
                .forEach(l -> {
                    if (destCount.containsKey(l))
                        destCount.put(l, destCount.get(l) + 1);
                    else
                        destCount.put(l, 1);
                });
        return destCount;
    }

    /** @return {@link Map} containing all {@link Link}s with staying {@link RoboTaxi} and a
     *         {@link Set} of all staying {@link RoboTaxi} on these links based
     *         on a set of {@link RoboTaxi}s @param stayRoboTaxis */
    public static Map<Link, Set<RoboTaxi>> getOccupiedLinks(Collection<RoboTaxi> stayingRobotaxis) {
        /** create empty map */
        Map<Link, Set<RoboTaxi>> stayTaxis = new HashMap<>();
        /** add all links that will occur */
        stayingRobotaxis.stream().map(rt -> rt.getDivertableLocation())//
                .forEach(l -> stayTaxis.put(l, new HashSet<RoboTaxi>()));
        /** associate RoboTaxis to links */
        stayingRobotaxis.stream().forEach(rt -> stayTaxis.get(rt.getDivertableLocation()).add(rt));
        return stayTaxis;
    }

}
