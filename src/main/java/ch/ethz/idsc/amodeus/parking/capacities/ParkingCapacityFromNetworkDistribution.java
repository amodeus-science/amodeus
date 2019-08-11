/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.capacities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

public class ParkingCapacityFromNetworkDistribution extends ParkingCapacityAbstract {

    /** Randomly choses @param numberSpots from the @param network out of all links
     * that have parking spaces marked with @param capacityIdentifier, random selection
     * done using the {@link Random} @param random */
    public ParkingCapacityFromNetworkDistribution(Network network, String capacityIdentifier, //
            Random random, long desiredTotalSpots) {

        /** get a list of all parkings, if a Link has multiple spots, it will
         * show up in the list multiple times */
        List<Link> linksWithParking = new ArrayList<>();
        network.getLinks().values().stream()//
                .forEach(l -> {
                    try {
                        Map<String, Object> attrMap = l.getAttributes().getAsMap();
                        if (attrMap.containsKey(capacityIdentifier)) {
                            Object attribute = attrMap.get(capacityIdentifier);
                            Long capacity = Long.parseLong(attribute.toString());
                            if (capacity > 0) {
                                for (int i = 0; i < capacity; ++i)
                                    linksWithParking.add(l);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        // TODO solve more elegantly
                    }
                });

        /** assign according to distribution */
        Map<Id<Link>, Long> parkingCount = new HashMap<>();
        int bound = linksWithParking.size();
        for (int i = 0; i < desiredTotalSpots; ++i) {
            int elemRand = random.nextInt(bound);
            Link link = linksWithParking.stream().skip(elemRand).findFirst().get();
            if (!parkingCount.containsKey(link.getId()))
                parkingCount.put(link.getId(), (long) 0);
            parkingCount.put(link.getId(), parkingCount.get(link.getId()) + 1);
        }
        parkingCount.entrySet().stream().forEach(e -> {
            capacities.put(e.getKey(), e.getValue());
        });
    }
}