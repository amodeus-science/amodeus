/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacity;

@FunctionalInterface
public interface ParkingCapacityGenerator {

    /** @returns a {@link ParkingCapacity} for a given {@link Network} @param network.
     *          Some generators require additional {@link ScenarioOption}s @param scenarioOptions
     * 
     *          Generation could for example be by searching for a given link attribute in the network
     *          or by using the length of the link as a indication of its capacity. */
    public ParkingCapacity generate(Network network, ScenarioOptions scenarioOptions);

}
