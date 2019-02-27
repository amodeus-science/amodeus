package ch.ethz.idsc.amodeus.dispatcher.parking.strategies;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.parking.AVSpatialCapacityAmodeus;

class ParkingAdvancedDiffusion implements ParkingStrategy {

    private static final long RANDOMSEED = 1234;

    private final long freeParkingPeriod = 5;

    private final AVSpatialCapacityAmodeus avSpatialCapacityAmodeus;
    private final Random random;

    ParkingAdvancedDiffusion(AVSpatialCapacityAmodeus avSpatialCapacityAmodeus) {
        this.avSpatialCapacityAmodeus = avSpatialCapacityAmodeus;
        this.random = new Random(RANDOMSEED);
    }

    @Override
    public Map<RoboTaxi, Link> keepFree(Collection<RoboTaxi> stayingRobotaxis, Collection<RoboTaxi> rebalancingRobotaxis, long now) {

        if (now % freeParkingPeriod == 0) {

            Map<Link, Set<RoboTaxi>> currCount = getOccupiedLinks(stayingRobotaxis);
            ParkingAdvancedDiffusionHelper parkingAdvancedDiffusionHelper = new ParkingAdvancedDiffusionHelper(avSpatialCapacityAmodeus, stayingRobotaxis, rebalancingRobotaxis,
                    random);

            Map<RoboTaxi, Link> directives = new HashMap<>();

            currCount.entrySet().stream()//
                    .forEach(linkTaxiPair -> {
                        if (linkTaxiPair.getValue().size() > avSpatialCapacityAmodeus.getSpatialCapacity(linkTaxiPair.getKey().getId()) * 0.5) {
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
