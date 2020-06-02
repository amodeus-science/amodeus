/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.prep;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import amodeus.amodeus.options.ScenarioOptions;
import amodeus.amodeus.traveldata.TravelData;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;

@FunctionalInterface
public interface VirtualNetworkCreator {
    /** creates a {@link VirtualNetwork} based on the given parameters
     *
     * @param network {@link Network}
     * @param population {@link Population}
     * @param scenarioOptions {@link ScenarioOptions}
     * @param numRoboTaxis
     * @param endTime of simulation for {@link TravelData}
     * @return virtual network */
    VirtualNetwork<Link> create(Network network, Population population, ScenarioOptions scenarioOptions, int numRoboTaxis, int endTime);
}
