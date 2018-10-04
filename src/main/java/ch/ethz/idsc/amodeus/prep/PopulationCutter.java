/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.io.IOException;
import java.net.MalformedURLException;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;

public interface PopulationCutter {
    // TODO document
    // TODO LUKAS my suggestion is that this function does not throw any exception ...
    // ... any exception internal to a cutter may be passed on as a RuntimeException
    void cut(Population population, Network network, Config config) throws MalformedURLException, IOException, Exception;
}
