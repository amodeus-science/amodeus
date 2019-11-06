/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/* package */ enum StaticHelper {
    ;

    /** @return {@link Map} with {@link Link}s that {@link RoboTaxi}s are driving towards
     *         and for each {@link Link} the number of {@link RoboTaxi}s driving there based on a
     *         {@link Collection} of {@link RoboTaxi}s @param roboTaxis */
    public static Map<Link, Integer> getDestinationCount(Collection<RoboTaxi> roboTaxis) {
        return roboTaxis.stream().collect(Collectors.toMap(RoboTaxi::getCurrentDriveDestination, rt -> 1, Integer::sum));
    }

    /** @return {@link Map} containing all {@link Link}s with staying {@link RoboTaxi} and a
     *         {@link Set} of all staying {@link RoboTaxi} on these links based
     *         on a set of {@link RoboTaxi}s @param stayRoboTaxis */
    public static Map<Link, Set<RoboTaxi>> getOccupiedLinks(Collection<RoboTaxi> stayingRobotaxis) {
        return stayingRobotaxis.stream().collect(Collectors.groupingBy(RoboTaxi::getDivertableLocation, Collectors.toSet()));
    }
}
