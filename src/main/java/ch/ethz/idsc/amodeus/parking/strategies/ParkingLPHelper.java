/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacity;

/* package */ class ParkingLPHelper {

    private final ParkingCapacity parkingCapacity;
    private final Network network;
    private final static double BOUNDCAPACITY = 0.5; // TODO remove magic const.

    public ParkingLPHelper(ParkingCapacity parkingCapacity, Network network) {
        this.parkingCapacity = parkingCapacity;
        this.network = network;
    }

    public Map<Link, Long> getFreeSpacesToGo(Map<Link, Set<RoboTaxi>> linkStayTaxi, Collection<RoboTaxi> rebalancingRoboTaxis) {
        Map<Link, Long> freeSpacesToGo = new HashMap<>();
        Map<Link, Integer> linkRebalancingTaxi = StaticHelper.getDestinationCount(rebalancingRoboTaxis);
        for (Link link : network.getLinks().values()) {
            Long freeSpaces = (long) Math.ceil(BOUNDCAPACITY * parkingCapacity.getSpatialCapacity(link.getId()));
            if (linkStayTaxi.containsKey(link)) {
                freeSpaces = (long) Math.ceil(BOUNDCAPACITY * parkingCapacity.getSpatialCapacity(link.getId())//
                        - linkStayTaxi.get(link).size());
            } else if (linkRebalancingTaxi.containsKey(link)) {
                freeSpaces = (long) Math.ceil(BOUNDCAPACITY * parkingCapacity.getSpatialCapacity(link.getId())//
                        - linkRebalancingTaxi.get(link));
            } else if ((linkRebalancingTaxi.containsKey(link)) & (linkStayTaxi.containsKey(link))) {
                freeSpaces = (long) Math.ceil(BOUNDCAPACITY * parkingCapacity.getSpatialCapacity(link.getId())//
                        - linkRebalancingTaxi.get(link) - linkStayTaxi.get(link).size());
            }
            if (freeSpaces > 0) {
                freeSpacesToGo.put(link, freeSpaces);
            }
        }
        return freeSpacesToGo;
    }

    public Map<Link, Set<RoboTaxi>> getTaxisToGo(Map<Link, Set<RoboTaxi>> linkStayTaxi) {
        Map<Link, Set<RoboTaxi>> shouldLeaveTaxis = new HashMap<>();
        for (Link link : linkStayTaxi.keySet()) {
            if (linkStayTaxi.get(link).size() > BOUNDCAPACITY * parkingCapacity.getSpatialCapacity(link.getId())) {
                linkStayTaxi.get(link).stream()//
                        .limit(Math.round(BOUNDCAPACITY * linkStayTaxi.get(link).size()))//
                        .forEach(rt -> {
                            if (!shouldLeaveTaxis.containsKey(link))
                                shouldLeaveTaxis.put(link, new HashSet<RoboTaxi>());
                            shouldLeaveTaxis.get(link).add(rt);
                        });
            }
        }
        return shouldLeaveTaxis;
    }
}