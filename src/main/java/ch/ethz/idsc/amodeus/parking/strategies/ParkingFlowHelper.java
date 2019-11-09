/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacity;

/* package */ class ParkingFlowHelper {

    private final ParkingCapacity parkingCapacity;
    private final Network network;

    public ParkingFlowHelper(ParkingCapacity parkingCapacity, Network network) {
        this.parkingCapacity = parkingCapacity;
        this.network = network;
    }

    /** @return {@link Map} on {@link Link} keys with the {@link Set}s of {@link T}s that must
     *         leave in order not to violate parking constraints.
     *         Input are the currently staying {@link T}s @param linkStayTaxi sorted in a map */
    public <T> Map<Link, Set<T>> getTaxisToGo(Map<Link, Set<T>> linkStayTaxi) {
        Map<Link, Set<T>> shouldLeaveTaxis = new HashMap<>();
        for (Link link : linkStayTaxi.keySet()) {
            double nominalCapacity = parkingCapacity.getSpatialCapacity(link.getId());
            Integer nominalSpaces = (int) Math.ceil(nominalCapacity);
            Integer numTaxis = linkStayTaxi.get(link).size();
            if (numTaxis > nominalSpaces) {
                int shouldLeave = Math.max(0, numTaxis - nominalSpaces);
                for (T rt : linkStayTaxi.get(link)) {
                    shouldLeaveTaxis.computeIfAbsent(link, l -> new HashSet<>()) //
                            /* shouldLeaveTaxis.get(link) */ .add(rt);
                    shouldLeave--;
                    if (shouldLeave < 1)
                        break;
                }
            }
        }
        return shouldLeaveTaxis;
    }

    /** @return {@link Map} with currently available {@link Link}s that have parking
     *         spaces and the respective number given the staying {@link T}s @param linkStayTaxi
     *         and the rebalancing {@link T}s @param linkRebalancingTaxi */
    public <T> Map<Link, Integer> getFreeSpacesToGo(Map<Link, Set<T>> linkStayTaxi, //
            Map<Link, Integer> linkRebalancingTaxi) {
        Map<Link, Integer> freeSpacesToGo = new HashMap<>();
        for (Link link : network.getLinks().values()) {
            double nominalCapacity = parkingCapacity.getSpatialCapacity(link.getId());
            Integer nominalSpaces = (int) Math.ceil(nominalCapacity);
            Integer stayTaxis = linkStayTaxi.containsKey(link) ? linkStayTaxi.get(link).size() : 0;
            Integer reblTaxis = linkRebalancingTaxi.getOrDefault(link, 0);
            Integer freeSpaces = Math.max(0, nominalSpaces - stayTaxis - reblTaxis);
            if (freeSpaces > 0)
                freeSpacesToGo.put(link, freeSpaces);
        }
        return freeSpacesToGo;
    }
}