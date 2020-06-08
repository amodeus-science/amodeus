/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.parking;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import amodeus.amodeus.options.ScenarioOptions;
import amodeus.amodeus.parking.capacities.ParkingCapacity;

@FunctionalInterface
public interface ParkingCapacityGenerator {

    /** @returns a {@link ParkingCapacity} for a given {@link Network} @param network.
     *          Some generators require additional
     *          @param scenarioOptions {@link ScenarioOptions}
     * 
     *          Generation could for example be by searching for a given link attribute in the network
     *          or by using the length of the link as a indication of its capacity. */
    ParkingCapacity generate(Network network, Population population, ScenarioOptions scenarioOptions);

}
