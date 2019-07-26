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

/* package */ class ParkingDirectedDiffusionHelper {

    private final static double BOUNDCAPACITY = 0.5;

    private final ParkingCapacity avSpatialCapacityAmodeus;
    private final Random random;
    private Map<Link, Set<RoboTaxi>> occMap;

    ParkingDirectedDiffusionHelper(ParkingCapacity avSpatialCapacityAmodeus, Collection<RoboTaxi> stayingRobotaxis, Collection<RoboTaxi> rebalancingRobotaxis,
            Random random) {
        this.avSpatialCapacityAmodeus = avSpatialCapacityAmodeus;
        this.random = random;
        this.occMap = getOccMap(stayingRobotaxis, rebalancingRobotaxis);

    }

    Link getDestinationLink(RoboTaxi rt) {

        List<Link> firstNeighbors = new ArrayList<>(rt.getDivertableLocation().getToNode().getOutLinks().values());
        List<Link> secondNeighbors = getNeighborLinks(firstNeighbors, rt);

        NavigableMap<Long, Link> destMap = new TreeMap<>();

        firstNeighbors.stream().forEach(neighLink -> {
            Long freeSpaces = (long) Math.ceil(BOUNDCAPACITY * avSpatialCapacityAmodeus.getSpatialCapacity(neighLink.getId()));
            if (occMap.containsKey(neighLink)) {
                freeSpaces = (long) Math.ceil(BOUNDCAPACITY * avSpatialCapacityAmodeus.getSpatialCapacity(neighLink.getId())//
                        - occMap.get(neighLink).size());
            }

            if (freeSpaces > 0) {
                destMap.put(freeSpaces, neighLink);
            }
        });

        secondNeighbors.stream().forEach(neighLink -> {
            Long freeSpaces = (long) Math.ceil(BOUNDCAPACITY * avSpatialCapacityAmodeus.getSpatialCapacity(neighLink.getId()));
            if (occMap.containsKey(neighLink)) {
                freeSpaces = (long) Math.ceil(BOUNDCAPACITY * avSpatialCapacityAmodeus.getSpatialCapacity(neighLink.getId())//
                        - occMap.get(neighLink).size());
            }

            if (freeSpaces > 0) {
                destMap.put(freeSpaces, neighLink);
            }
        });

        if (destMap.isEmpty()) {
            Collections.shuffle(secondNeighbors, random);
            Link destination = secondNeighbors.get(0);
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

    private static Map<Link, Set<RoboTaxi>> getOccMap(Collection<RoboTaxi> stayingRobotaxis, Collection<RoboTaxi> rebalancingRobotaxis) {
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
