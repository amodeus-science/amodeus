/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.parking.capacities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import amodeus.amodeus.util.math.GlobalAssert;

public abstract class ParkingCapacityAbstractUniform extends ParkingCapacityAbstract {

    /** assigns totSpaces randomly chosen links from the network a parking space, there may
     * be multiple parking spaces per link */
    public ParkingCapacityAbstractUniform(Network network, Population population, long totSpaces, Random random) {
        fillUsingLinks(getLinks(network, population), totSpaces, random);
    }

    protected abstract Collection<? extends Link> getLinks(Network network, Population population);

    protected void fillUsingLinks(Collection<? extends Link> possibleLinks, //
            long totSpaces, Random random) {
        Map<Id<Link>, Long> parkingCount = new HashMap<>();
        int bound = possibleLinks.size();
        for (int i = 0; i < totSpaces; ++i) {
            int elemRand = random.nextInt(bound);
            Link link = possibleLinks.stream().skip(elemRand).findFirst().get();
            parkingCount.merge(link.getId(), 1L, Long::sum);
        }
        parkingCount.forEach(capacities::put);
        GlobalAssert.that(totSpaces == capacities.values().stream().mapToLong(l -> l).sum());
    }
}
