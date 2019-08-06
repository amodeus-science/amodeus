/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.capacities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class ParkingCapacityFromNetworkDistribution extends ParkingCapacityAbstract {

    /** Randomly choses @param numberSpots from the @param network out of all links
     * that have parking spaces marked with @param capacityIdentifier, random selection
     * done using the {@link Random} @param random */
    public ParkingCapacityFromNetworkDistribution(Network network, String capacityIdentifier, //
            Random random, long desiredTotalSpots) {

        List<Link> linksWithParking = new ArrayList<>();
        network.getLinks().values().stream()//
                .forEach(l -> {
                    try {
                        Map<String, Object> attrMap = l.getAttributes().getAsMap();
                        if (attrMap.containsKey(capacityIdentifier)) {
                            Object attribute = attrMap.get(capacityIdentifier);
                            Long capacity = Long.parseLong(attribute.toString());
                            if (capacity > 0)
                                linksWithParking.add(l);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        // TODO solve more elegantly
                    }
                });

        System.out.println("Number of links with Parking: " + linksWithParking.size());
        if (linksWithParking.size() < 1) {
            System.err.println("The network does not contain any parkings for identifier:");
            System.err.println(capacityIdentifier);
            System.err.println("Aborting.");
            GlobalAssert.that(false);
        }

        /** shuffle to have random selection of streets */
        Collections.shuffle(linksWithParking, random);
        long assignedSpots = 0;
        for (Link link : linksWithParking) {
            if (desiredTotalSpots == assignedSpots)
                break;
            Long streetCapacity = Long.parseLong(link.getAttributes().getAttribute(capacityIdentifier).toString());
            long putSpots = Math.min(streetCapacity, desiredTotalSpots - assignedSpots);
            assignedSpots += putSpots;
            capacities.put(link.getId(), putSpots);
        }
        System.out.println("Number of desired parking spots: " + desiredTotalSpots);
        System.out.println("Number of assigned parking spots: " + assignedSpots);
    }
}