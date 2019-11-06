/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.capacities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

public class ParkingCapacityNetworkDistribution extends ParkingCapacityAbstract {

    /** Randomly chooses @param numberSpots from the @param network out of all links
     * that have parking spaces marked with @param capacityIdentifier, random selection
     * done using the {@link Random} @param random */
    public ParkingCapacityNetworkDistribution(Network network, String capacityIdentifier, //
            Random random, long desiredTotalSpots) {

        /** get a list of all parkings, if a Link has multiple spots, it will
         * show up in the list multiple times */
        // List<Link> linksWithParking = new ArrayList<>();
        // network.getLinks().values().forEach(l -> { //
        //     try {
        //         Map<String, Object> attrMap = l.getAttributes().getAsMap();
        //         if (attrMap.containsKey(capacityIdentifier)) {
        //             Object attribute = attrMap.get(capacityIdentifier);
        //             Long capacity = Long.parseLong(attribute.toString());
        //             if (capacity > 0) {
        //                 for (int i = 0; i < capacity; ++i)
        //                     linksWithParking.add(l);
        //             }
        //         }
        //     } catch (Exception ex) {
        //         ex.printStackTrace();
        //         // TODO solve more elegantly
        //     }
        // });
        List<Link> linksWithParking = network.getLinks().values().stream().flatMap(link -> {
            Map<String, Object> attrMap = link.getAttributes().getAsMap();
            try {
                if (attrMap.containsKey(capacityIdentifier)) {
                    long capacity = Long.parseLong(attrMap.get(capacityIdentifier).toString());
                    return LongStream.range(0, capacity).mapToObj(l -> link);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Stream.empty();
        }).collect(Collectors.toList());

        /** assign according to distribution */
        Map<Id<Link>, Long> parkingCount = new HashMap<>();
        int bound = linksWithParking.size();
        LongStream.range(0, desiredTotalSpots).mapToInt(l -> random.nextInt(bound)).mapToObj(linksWithParking::get)
                .forEach(link -> parkingCount.merge(link.getId(), 1L, Long::sum));
        parkingCount.forEach(capacities::put);
    }
}