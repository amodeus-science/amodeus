/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.capacities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

public abstract class ParkingCapacityAbstractUniform extends ParkingCapacityAbstract {

    /** assigns totSpaces randomly chosen links from the network a parking space, there may
     * be multiple parking spaces per link */
    public ParkingCapacityAbstractUniform(Network network, Population population, long totSpaces, Random random) {
        Collection<? extends Link> possibleLinks = //
                getLinks(network, population);
        fillUsingLinks(possibleLinks, totSpaces, random);
    }

    protected abstract Collection<? extends Link> getLinks(Network network, Population population);

    protected void fillUsingLinks(Collection<? extends Link> possibleLinks, //
            long totSpaces, Random random) {
        Map<Id<Link>, Long> parkingCount = new HashMap<>();
        int bound = possibleLinks.size();
        for (int i = 0; i < totSpaces; ++i) {
            int elemRand = random.nextInt(bound);
            Link link = possibleLinks.stream().skip(elemRand).findFirst().get();
            if (!parkingCount.containsKey(link.getId()))
                parkingCount.put(link.getId(), (long) 0);
            parkingCount.put(link.getId(), parkingCount.get(link.getId()) + 1);
        }
        parkingCount.entrySet().stream().forEach(e -> {
            capacities.put(e.getKey(), e.getValue());
        });
        
        long total = capacities.values().stream().mapToLong(l->l).sum();
        System.out.println("the total is: " +  total);
        System.exit(1);
        System.out.println();
    }
}
