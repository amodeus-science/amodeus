/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    public ParkingMaintainer(AVSpatialCapacityAmodeus spatialCapacity) {
        this.spatialCapacity = spatialCapacity;
    }

    public Map<RoboTaxi, Link> keepFree(Collection<RoboTaxi> stayingRobotaxis) {
        Map<Link, Set<RoboTaxi>> currCount = new HashMap<>();

        /** count current robotaxis */
        stayingRobotaxis.forEach(rt -> {
            Link link = rt.getDivertableLocation();
            if (currCount.containsKey(link)) {
                currCount.get(link).add(rt);
            } else {
                Set<RoboTaxi> set = new HashSet<>();
                set.add(rt);
                currCount.put(link, set);
            }
        });

        /** if above flush threshold, then flush the entire link */
        Map<RoboTaxi, Link> directives = new HashMap<>();
        currCount.entrySet().stream().forEach(e -> {
            if (e.getValue().size() > spatialCapacity.getSpatialCapacity(e.getKey().getId()) * 0.5) {
                e.getValue().stream().//
                limit((long) Math.round(e.getValue().size() * 0.5)).//
                forEach(rt -> {
                    directives.put(rt, getRandomLink(e.getKey()));
                });
            }
        });
        return directives;
    }

    private Link getRandomLink(Link link) {
        Random random = new Random();
        List<Link> links = new ArrayList<>(link.getToNode().getOutLinks().values());
        Collections.shuffle(links, random);
        return links.get(0);
    }
}
