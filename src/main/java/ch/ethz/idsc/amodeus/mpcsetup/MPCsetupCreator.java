package ch.ethz.idsc.amodeus.mpcsetup;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;

public interface MPCsetupCreator {
    /** @param network
     * @param population
     * @param scenarioOptions
     * @param numRoboTaxis
     * @param endTime of simulation for {@link TravelData}
     * @return virtual network */
    MPCsetup create(ScenarioOptions scenarioOptions);
}
