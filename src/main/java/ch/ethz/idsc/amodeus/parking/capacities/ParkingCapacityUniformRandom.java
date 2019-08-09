/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.capacities;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

public class ParkingCapacityUniformRandom extends ParkingCapacityAbstract {

    /** assigns totSpaces randomly chosen links from the network a parking space, there may
     * be multiple parking spaces per link */
    public ParkingCapacityUniformRandom(Network network, long totSpaces, Random random) {
        int bound = network.getLinks().size();
        Map<Id<Link>, Long> parkingCount = new HashMap<>();
        for (int i = 0; i < totSpaces; ++i) {
            int elemRand = random.nextInt(bound);
            Link link = network.getLinks().values().stream().skip(elemRand).findFirst().get();
            if (!parkingCount.containsKey(link.getId()))
                parkingCount.put(link.getId(), (long) 0);
            parkingCount.put(link.getId(), parkingCount.get(link.getId()) + 1);
        }
        parkingCount.entrySet().stream().forEach(e -> {
            capacities.put(e.getKey(), e.getValue());
        });
    }
}
