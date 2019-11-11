/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacity;

// TODO have the number of degrees to search in as a variable 
/* package */ class DirectedDiffusionHelper {

    private final ParkingCapacity parkingCapacity;
    private final Random random;
    private Map<Link, Set<RoboTaxi>> occMap;

    public DirectedDiffusionHelper(ParkingCapacity parkingCapacity, //
            Collection<RoboTaxi> stayingRobotaxis, Collection<RoboTaxi> rebalancingRobotaxis, Random random) {
        this.parkingCapacity = parkingCapacity;
        this.random = random;
        this.occMap = getOccMap(stayingRobotaxis, rebalancingRobotaxis);
    }

    /** @return a {@link Link} for the {@link RoboTaxi} @param rt which is on a link with insufficient
     *         parking capacity */
    public Link getDestinationLink(RoboTaxi rt) {
        List<Link> deg1Neighbors = new ArrayList<>(rt.getDivertableLocation().getToNode().getOutLinks().values());
        List<Link> deg2Neighbors = getNeighborLinks(deg1Neighbors, rt);
        NavigableMap<Long, Link> destMap = new TreeMap<>();
        /** search possible destinations in degree 1 neighboring roads */
        deg1Neighbors.forEach(link -> {
            Long freeSpaces = parkingCapacity.getSpatialCapacity(link.getId());
            if (occMap.containsKey(link))
                freeSpaces = Math.max(0, freeSpaces - occMap.get(link).size());
            if (freeSpaces > 0)
                destMap.put(freeSpaces, link);
        });

        /** search possible destinations in degree 2 neighboring roads */
        deg2Neighbors.forEach(link -> {
            Long freeSpaces = parkingCapacity.getSpatialCapacity(link.getId());
            if (occMap.containsKey(link))
                freeSpaces = Math.max(0, freeSpaces - occMap.get(link).size());
            if (freeSpaces > 0)
                destMap.put(freeSpaces, link);
        });

        /** if there are no valid destinations, select a degree 2 neighbor at random */
        if (destMap.isEmpty()) {
            Collections.shuffle(deg2Neighbors, random);
            Link destination = deg2Neighbors.get(0);
            refreshOccMap(rt, destination);
            return destination;
        }
        return destMap.lastEntry().getValue();
    }

    private void refreshOccMap(RoboTaxi rt, Link destination) {
        // remove
        Link location = rt.getDivertableLocation();
        occMap.get(location).remove(rt);
        if (occMap.get(location).isEmpty())
            occMap.remove(location);
        // add
        occMap.computeIfAbsent(destination, l -> new HashSet<>()) //
                /* occMap.get(destination) */ .add(rt);
    }

    private static List<Link> getNeighborLinks(List<Link> firstNeighbors, RoboTaxi rt) {
        return firstNeighbors.stream().flatMap(link -> //
        link.getToNode().getOutLinks().values().stream().filter(l -> l != rt.getDivertableLocation())).distinct().collect(Collectors.toList());
    }

    private static Map<Link, Set<RoboTaxi>> getOccMap(Collection<RoboTaxi> stayingRobotaxis, //
            Collection<RoboTaxi> rebalancingRobotaxis) {
        // Map<Link, Set<RoboTaxi>> occMap = new HashMap<>();
        // for (RoboTaxi stayRT : stayingRobotaxis)
        // occMap.computeIfAbsent(stayRT.getDivertableLocation(), l -> new HashSet<>()).add(stayRT);
        Map<Link, Set<RoboTaxi>> occMap = stayingRobotaxis.stream().collect(Collectors.groupingBy(RoboTaxi::getDivertableLocation, Collectors.toSet()));
        for (RoboTaxi rebRT : rebalancingRobotaxis)
            occMap.computeIfAbsent(rebRT.getCurrentDriveDestination(), l -> new HashSet<>()).add(rebRT);
        return occMap;
    }
}
