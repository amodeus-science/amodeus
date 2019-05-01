/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;

@FunctionalInterface
public interface VirtualNetworkCreator {
    // TODO Joel document interface function
    /** @param network
     * @param population
     * @param scenarioOptions
     * @param numRoboTaxis
     * @param endTime of simulation for {@link TravelData}
     * @return virtual network */
    VirtualNetwork<Link> create(Network network, Population population, ScenarioOptions scenarioOptions, int numRoboTaxis, int endTime);
}
