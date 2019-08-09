/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

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

/* package */ class ParkingRandomDiffusion extends AbstractParkingStrategy {

    private final Random random;

    public ParkingRandomDiffusion(Random random) {
        this.random = random;
    }

    @Override
    public Map<RoboTaxi, Link> keepFree(Collection<RoboTaxi> stayingRobotaxis, //
            Collection<RoboTaxi> rebalancingRobotaxis, long now) {

        Map<Link, Set<RoboTaxi>> stayTaxis = new HashMap<>();

        /** Count the number of {@link RoboTaxi}s on all {@link Link}s */
        stayingRobotaxis.forEach(rt -> {
            Link link = rt.getDivertableLocation();
            if (stayTaxis.containsKey(link))
                stayTaxis.get(link).add(rt);
            else {
                Set<RoboTaxi> set = new HashSet<>();
                set.add(rt);
                stayTaxis.put(link, set);
            }
        });

        /** If there are too many vehicles on the link, send a sufficient number of them away
         * to random neighbors */
        Map<RoboTaxi, Link> directives = new HashMap<>();
        stayTaxis.entrySet().stream().forEach(e -> {
            Link link = e.getKey();
            Set<RoboTaxi> taxis = e.getValue();
            long capacity = parkingCapacity.getSpatialCapacity(link.getId());
            if (taxis.size() > capacity) {
                taxis.stream().limit(taxis.size() - capacity).//
                forEach(rt -> {
                    directives.put(rt, getRandomAdjacentLink(link));
                });
            }
        });
        return directives;

    }

    private Link getRandomAdjacentLink(Link link) {
        List<Link> links = new ArrayList<>(link.getToNode().getOutLinks().values());
        Collections.shuffle(links, random);
        return links.get(0);
    }

}
