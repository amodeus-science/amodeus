/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking.strategies;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.parking.ParkingCapacityAmodeus;

class ParkingLPStaticHelper {

    private final static double BOUNDCAPACITY = 0.5;

    private final ParkingCapacityAmodeus avSpatialCapacityAmodeus;
    private final Network network;

    ParkingLPStaticHelper(ParkingCapacityAmodeus avSpatialCapacityAmodeus, Network network) {
        this.avSpatialCapacityAmodeus = avSpatialCapacityAmodeus;
        this.network = network;
    }

    public Map<Link, Long> getFreeSpacesToGo(Map<Link, Set<RoboTaxi>> linkStayTaxi, Collection<RoboTaxi> rebalancingRoboTaxis) {
        Map<Link, Long> freeSpacesToGo = new HashMap<>();

        Map<Link, Integer> linkRebalancingTaxi = getRebalancingDestinations(rebalancingRoboTaxis);

        for (Link link : network.getLinks().values()) {
            Long freeSpaces = (long) Math.ceil(BOUNDCAPACITY * avSpatialCapacityAmodeus.getSpatialCapacity(link.getId()));
            if (linkStayTaxi.containsKey(link)) {
                freeSpaces = (long) Math.ceil(BOUNDCAPACITY * avSpatialCapacityAmodeus.getSpatialCapacity(link.getId())//
                        - linkStayTaxi.get(link).size());
            } else if (linkRebalancingTaxi.containsKey(link)) {
                freeSpaces = (long) Math.ceil(BOUNDCAPACITY * avSpatialCapacityAmodeus.getSpatialCapacity(link.getId())//
                        - linkRebalancingTaxi.get(link));
            } else if ((linkRebalancingTaxi.containsKey(link)) & (linkStayTaxi.containsKey(link))) {
                freeSpaces = (long) Math.ceil(BOUNDCAPACITY * avSpatialCapacityAmodeus.getSpatialCapacity(link.getId())//
                        - linkRebalancingTaxi.get(link) - linkStayTaxi.get(link).size());
            }
            if (freeSpaces > 0) {
                freeSpacesToGo.put(link, freeSpaces);
            }
        }

        return freeSpacesToGo;
    }

    public Map<Link, Set<RoboTaxi>> getTaxisToGo(Map<Link, Set<RoboTaxi>> linkStayTaxi) {
        Map<Link, Set<RoboTaxi>> result = new HashMap<>();
        for (Link link : linkStayTaxi.keySet()) {
            if (linkStayTaxi.get(link).size() > BOUNDCAPACITY * avSpatialCapacityAmodeus.getSpatialCapacity(link.getId())) {
                linkStayTaxi.get(link).stream().limit(Math.round(BOUNDCAPACITY * linkStayTaxi.get(link).size())).forEach(rt -> {
                    result.put(link, addRoboTaxiToSet(link, rt, result));
                });
            }
        }

        return result;
    }

    public Map<Link, Set<RoboTaxi>> getOccupiedLinks(Collection<RoboTaxi> stayRoboTaxis) {
        /** returns a map with a link as key and the set of robotaxis which are currently on the link */
        Map<Link, Set<RoboTaxi>> rtLink = new HashMap<>();

        stayRoboTaxis.forEach(rt -> {
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

    private static Set<RoboTaxi> addRoboTaxiToSet(Link link, RoboTaxi rt, Map<Link, Set<RoboTaxi>> result) {
        if (result.containsKey(link)) {
            result.get(link).add(rt);
            return result.get(link);
        }
        Set<RoboTaxi> set = new HashSet<>();
        set.add(rt);
        return set;
    }

    private static Map<Link, Integer> getRebalancingDestinations(Collection<RoboTaxi> rebalancingTaxis) {
        Map<Link, Integer> result = new HashMap<>();
        rebalancingTaxis.stream().forEach(rt -> {
            Link link = rt.getCurrentDriveDestination();
            if (result.containsKey(link)) {
                result.replace(link, result.get(link) + 1);
            } else {
                result.put(link, 1);
            }
        });
        return result;
    }

}
