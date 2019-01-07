/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

public class ParkingMaintainer {
    private static final double FRACTION = 0.5; // magic constant
    // ---
    private final Random random = new Random();
    private final AVSpatialCapacityAmodeus spatialCapacity;

    public ParkingMaintainer(AVSpatialCapacityAmodeus spatialCapacity) {
        this.spatialCapacity = Objects.requireNonNull(spatialCapacity);
    }

    public Map<RoboTaxi, Link> keepFree(Collection<RoboTaxi> stayingRobotaxis) {
        Map<Link, Set<RoboTaxi>> currCount = new HashMap<>();

        /** count current robotaxis */
        stayingRobotaxis.forEach(roboTaxi -> {
            Link link = roboTaxi.getDivertableLocation();
            if (currCount.containsKey(link))
                currCount.get(link).add(roboTaxi);
            else {
                Set<RoboTaxi> set = new HashSet<>();
                set.add(roboTaxi);
                currCount.put(link, set);
            }
        });

        /** if above flush threshold, then flush the entire link */
        Map<RoboTaxi, Link> directives = new HashMap<>();
        currCount.entrySet().stream().forEach(entry -> {
            int taxiCount = entry.getValue().size();
            if (spatialCapacity.getSpatialCapacity(entry.getKey().getId()) * FRACTION < taxiCount)
                entry.getValue().stream() //
                        .limit(Math.round(taxiCount * FRACTION)) //
                        .forEach(roboTaxi -> directives.put(roboTaxi, getOutLinkRandom(entry.getKey())));
        });
        return directives;
    }

    private Link getOutLinkRandom(Link link) {
        // if (!alternativeLinks.containsKey(link))
        // alternativeLinks.put(link, new ArrayList<>(link.getToNode().getOutLinks().values()));
        // List<Link> links = alternativeLinks.get(link);
        // return links.get(random.nextInt(links.size()));
        return StaticHelper.randomElement(link.getToNode().getOutLinks().values(), random);
    }
}
