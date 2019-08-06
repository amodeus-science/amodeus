/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/* package */ class ParkingDirectedDiffusion extends ParkingStrategyWithCapacity {

    private final long freeParkingPeriod = 5;
    private final Random random;

    ParkingDirectedDiffusion(Random random) {
        this.random = random;
    }

    @Override
    public Map<RoboTaxi, Link> keepFree(Collection<RoboTaxi> stayingRobotaxis, //
            Collection<RoboTaxi> rebalancingRobotaxis, long now) {
        if (now % freeParkingPeriod == 0) {
            Map<Link, Set<RoboTaxi>> currCount = getOccupiedLinks(stayingRobotaxis);
            ParkingDirectedDiffusionHelper parkingAdvancedDiffusionHelper = //
                    new ParkingDirectedDiffusionHelper(parkingCapacity, stayingRobotaxis, rebalancingRobotaxis, random);
            Map<RoboTaxi, Link> directives = new HashMap<>();
            currCount.entrySet().stream()//
                    .forEach(linkTaxiPair -> {
                        if (linkTaxiPair.getValue().size() > parkingCapacity.getSpatialCapacity(linkTaxiPair.getKey().getId()) * 0.5) {
                            linkTaxiPair.getValue().stream()//
                                    .limit(Math.round(linkTaxiPair.getValue().size() * 0.5))//
                                    .forEach(rt -> {
                                        directives.put(rt, parkingAdvancedDiffusionHelper.getDestinationLink(rt));
                                    });
                        }
                    });
            return directives;
        }
        return new HashMap<>();
    }

    private static Map<Link, Set<RoboTaxi>> getOccupiedLinks(Collection<RoboTaxi> stayingRobotaxis) {
        /** returns a map with a link as key and the set of robotaxis which are currently on the link */
        Map<Link, Set<RoboTaxi>> rtLink = new HashMap<>();

        stayingRobotaxis.forEach(rt -> {
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
}
