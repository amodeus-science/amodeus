/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking.strategies;

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

/* package */ class ParkingRandomDiffusion extends ParkingStrategyWithCapacity {

    private final long freeParkingPeriod = 5;
    private final Random random;

    public ParkingRandomDiffusion(Random random) {
        this.random = random;
    }

    @Override
    public Map<RoboTaxi, Link> keepFree(Collection<RoboTaxi> stayingRobotaxis, Collection<RoboTaxi> rebalancingRobotaxis, long now) {
        if (now % freeParkingPeriod == 0) {

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
                if (e.getValue().size() > parkingCapacity.getSpatialCapacity(e.getKey().getId()) * 0.5) {
                    e.getValue().stream().//
                    limit(Math.round(e.getValue().size() * 0.5)).//
                    forEach(rt -> {
                        directives.put(rt, getRandomLink(e.getKey()));
                    });
                }
            });
            return directives;
        }
        return new HashMap<>();
    }

    private Link getRandomLink(Link link) {
        List<Link> links = new ArrayList<>(link.getToNode().getOutLinks().values());
        Collections.shuffle(links, random);
        return links.get(0);
    }

}
