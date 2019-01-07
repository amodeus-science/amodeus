/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

public class ParkingMaintainer {

    private final AVSpatialCapacityAmodeus spatialCapacity;

    private final Map<Link, List<Link>> alternativeLinks = new HashMap<>();

    private final Random random = new Random();

    public ParkingMaintainer(AVSpatialCapacityAmodeus spatialCapacity) {
        this.spatialCapacity = spatialCapacity;
    }

    public Map<RoboTaxi, Link> keepFree(Collection<RoboTaxi> stayingRobotaxis) {
        Map<Link, Set<RoboTaxi>> currCount = new HashMap<>();

        /** count current robotaxis */
        stayingRobotaxis.forEach(roboTaxi -> {
            Link link = roboTaxi.getDivertableLocation();
            if (currCount.containsKey(link)) {
                currCount.get(link).add(roboTaxi);
            } else {
                Set<RoboTaxi> set = new HashSet<>();
                set.add(roboTaxi);
                currCount.put(link, set);
            }
        });

        /** if above flush threshold, then flush the entire link */
        // TODO very inefficient since getRandomLink (which builds a list) is called quite often...
        // TODO Jan: does it help enough to store the list in a map and draw from that list instead of building it all the time? What would be anther option?
        Map<RoboTaxi, Link> directives = new HashMap<>();
        currCount.entrySet().stream().forEach(entry -> {
            if (entry.getValue().size() > spatialCapacity.getSpatialCapacity(entry.getKey().getId()) * 0.5) {
                entry.getValue().stream() //
                        .limit(Math.round(entry.getValue().size() * 0.5)) //
                        .forEach(rt -> directives.put(rt, getRandomLink(entry.getKey())));
            }
        });
        return directives;
    }

    private Link getRandomLink(Link link) {
        if (!alternativeLinks.containsKey(link)) {
            alternativeLinks.put(link, new ArrayList<>(link.getToNode().getOutLinks().values()));
        }
        List<Link> links = alternativeLinks.get(link);
        return links.get(random.nextInt(links.size()));
    }
}
