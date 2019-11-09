/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

@Deprecated
// TODO CLRUCH not used, remove if sure not needed.
/* package */ class ParkingHelper {
    private final Collection<RoboTaxi> allRoboTaxis;
    private final Map<Link, Set<RoboTaxi>> occLinks;

    ParkingHelper(Collection<RoboTaxi> allRoboTaxis) {
        this.allRoboTaxis = allRoboTaxis;
        this.occLinks = getOccupiedLinks();
    }

    private Map<Link, Set<RoboTaxi>> getOccupiedLinks() {
        /** returns a map with a link as key and the set of robotaxis which are currently on the link */
        // Map<Link, Set<RoboTaxi>> rtLink = new HashMap<>();
        // allRoboTaxis.forEach(rt -> rtLink.computeIfAbsent(rt.getDivertableLocation(), l -> new HashSet<>()).add(rt));
        // return rtLink;
        return allRoboTaxis.stream().collect(Collectors.groupingBy(RoboTaxi::getDivertableLocation, Collectors.toSet()));
    }

    public Map<Link, Set<RoboTaxi>> getLinksWithTaxis() {
        return occLinks;
    }
}
