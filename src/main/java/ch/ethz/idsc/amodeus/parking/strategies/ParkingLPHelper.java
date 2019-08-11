/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

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

    public ParkingLPHelper(ParkingCapacity parkingCapacity, Network network) {
        this.parkingCapacity = parkingCapacity;
        this.network = network;
    }

    /** @return {@link Map} on {@link Link} keys with the {@link Set}s of {@link RoboTaxi}s that must
     *         leave in order not to violate parking constraints.
     *         Input are the currently staying {@link RoboTaxi}s @param linkStayTaxi sorted in a map */
    public Map<Link, Set<RoboTaxi>> getTaxisToGo(Map<Link, Set<RoboTaxi>> linkStayTaxi) {
        Map<Link, Set<RoboTaxi>> shouldLeaveTaxis = new HashMap<>();
        for (Link link : linkStayTaxi.keySet()) {
            double nominalCapacity = parkingCapacity.getSpatialCapacity(link.getId());
            Integer nominalSpaces = (int) Math.ceil(nominalCapacity);
            Integer numTaxis = linkStayTaxi.get(link).size();
            if (numTaxis > nominalCapacity) {
                Integer shouldLeave = Math.max(0, numTaxis - nominalSpaces);
                for (RoboTaxi rt : linkStayTaxi.get(link)) {
                    if (!shouldLeaveTaxis.containsKey(link))
                        shouldLeaveTaxis.put(link, new HashSet<RoboTaxi>());
                    shouldLeaveTaxis.get(link).add(rt);
                    shouldLeave--;
                    if (shouldLeave < 1)
                        break;
                }
            }
        }
        return shouldLeaveTaxis;
    }

    /** @return {@link Map} with currently available {@link Link}s that have parking
     *         spaces and the respective number given the staying {@link RoboTaxi}s @param linkStayTaxi
     *         and the rebalancing {@link RoboTaxi}s @param linkRebalancingTaxi */
    public Map<Link, Long> getFreeSpacesToGo(Map<Link, Set<RoboTaxi>> linkStayTaxi, //
            Map<Link, Long> linkRebalancingTaxi) {
        Map<Link, Long> freeSpacesToGo = new HashMap<>();
        for (Link link : network.getLinks().values()) {
            double nominalCapacity = parkingCapacity.getSpatialCapacity(link.getId());
            Long nominalSpaces = (long) Math.ceil(nominalCapacity);
            Long stayTaxis = linkStayTaxi.containsKey(link) ? (long) linkStayTaxi.get(link).size() : (long) 0;
            Long reblTaxis = linkRebalancingTaxi.containsKey(link) ? linkRebalancingTaxi.get(link) : (long) 0;
            Long freeSpaces = Math.max(0, nominalSpaces - stayTaxis - reblTaxis);
            if (freeSpaces > 0)
                freeSpacesToGo.put(link, freeSpaces);
        }
        return freeSpacesToGo;
    }
}