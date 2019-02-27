package ch.ethz.idsc.amodeus.dispatcher.parking.strategies;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

class ParkingHelper {

    private final Collection<RoboTaxi> allRoboTaxis;
    private final Map<Link, Set<RoboTaxi>> occLinks;

    ParkingHelper(Collection<RoboTaxi> allRoboTaxis) {
        this.allRoboTaxis = allRoboTaxis;
        this.occLinks = getOccupiedLinks();
    }

    private Map<Link, Set<RoboTaxi>> getOccupiedLinks() {
        /** returns a map with a link as key and the set of robotaxis which are currently on the link */
        Map<Link, Set<RoboTaxi>> rtLink = new HashMap<>();

        allRoboTaxis.forEach(rt -> {
            Link link = rt.getDivertableLocation();
            if (rtLink.containsKey(link)) {
                rtLink.get(link).add(rt);
            } else {
                Set<RoboTaxi> set = new HashSet<>();
                set.add(rt);
                rtLink.put(link, set);
            }
        });
        return rtLink;
    }

    public Map<Link, Set<RoboTaxi>> getLinksWithTaxis() {

        return occLinks;
    }

}
