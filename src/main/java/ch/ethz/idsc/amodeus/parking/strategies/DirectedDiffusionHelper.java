/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

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
        deg1Neighbors.stream()//
                .forEach(link -> {
                    Long freeSpaces = parkingCapacity.getSpatialCapacity(link.getId());
                    if (occMap.containsKey(link))
                        freeSpaces = Math.max(0, freeSpaces - occMap.get(link).size());
                    if (freeSpaces > 0) {
                        destMap.put(freeSpaces, link);
                    }
                });

        /** search possible destinations in degree 2 neighboring roads */
        deg2Neighbors.stream().forEach(link -> {
            Long freeSpaces = parkingCapacity.getSpatialCapacity(link.getId());
            if (occMap.containsKey(link))
                freeSpaces = Math.max(0, freeSpaces - occMap.get(link).size());
            if (freeSpaces > 0) {
                destMap.put(freeSpaces, link);
            }
        });

        /** if there are no valid destinations, select a degree 2 neighbor at random */
        if (destMap.isEmpty()) {
            Collections.shuffle(deg2Neighbors, random);
            Link destination = deg2Neighbors.get(0);
            refreshOccMap(rt, destination);
            return destination;
        }
        Link destination = destMap.lastEntry().getValue();
        return destination;
    }

    private void refreshOccMap(RoboTaxi rt, Link destination) {
        // remove
        Link location = rt.getDivertableLocation();
        occMap.get(location).remove(rt);
        if (occMap.get(location).isEmpty()) {
            occMap.remove(location);
        }
        // add
        if (!occMap.containsKey(destination)) {
            occMap.put(destination, new HashSet<>());
        }
        occMap.get(destination).add(rt);
    }

    private static List<Link> getNeighborLinks(List<Link> firstNeighbors, RoboTaxi rt) {
        List<Link> secondNeighbors = new ArrayList<>();
        for (Link link : firstNeighbors) {
            List<Link> newNeighbors = new ArrayList<>(link.getToNode().getOutLinks().values());
            for (Link link2 : newNeighbors) {
                if ((!secondNeighbors.contains(link2)) & (rt.getDivertableLocation() != link2)) {
                    secondNeighbors.add(link2);
                }
            }
        }
        return secondNeighbors;
    }

    private static Map<Link, Set<RoboTaxi>> getOccMap(Collection<RoboTaxi> stayingRobotaxis, //
            Collection<RoboTaxi> rebalancingRobotaxis) {
        Map<Link, Set<RoboTaxi>> occMap = new HashMap<>();

        for (RoboTaxi stayRT : stayingRobotaxis) {
            if (occMap.containsKey(stayRT.getDivertableLocation())) {
                Set<RoboTaxi> currSet = occMap.get(stayRT.getDivertableLocation());
                currSet.add(stayRT);
                occMap.replace(stayRT.getDivertableLocation(), currSet);
            } else {
                Set<RoboTaxi> newSet = new HashSet<>();
                newSet.add(stayRT);
                occMap.put(stayRT.getDivertableLocation(), newSet);
            }
        }

        for (RoboTaxi rebRT : rebalancingRobotaxis) {
            if (occMap.containsKey(rebRT.getCurrentDriveDestination())) {
                Set<RoboTaxi> currSet = occMap.get(rebRT.getCurrentDriveDestination());
                currSet.add(rebRT);
                occMap.replace(rebRT.getCurrentDriveDestination(), currSet);
            } else {
                Set<RoboTaxi> newSet = new HashSet<>();
                newSet.add(rebRT);
                occMap.put(rebRT.getCurrentDriveDestination(), newSet);
            }
        }
        return occMap;
    }
}
