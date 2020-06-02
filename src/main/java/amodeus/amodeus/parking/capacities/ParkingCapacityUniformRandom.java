/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.parking.capacities;

import java.util.Collection;
import java.util.Random;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

public class ParkingCapacityUniformRandom extends ParkingCapacityAbstractUniform {

    /** assigns totSpaces randomly chosen links from the network a parking space, there may
     * be multiple parking spaces per link */
    public ParkingCapacityUniformRandom(Network network, Population population, //
            long totSpaces, Random random) {
        super(network, population, totSpaces, random);
    }

    @Override
    protected Collection<? extends Link> getLinks(Network network, Population population) {
        return network.getLinks().values();
    }
}
