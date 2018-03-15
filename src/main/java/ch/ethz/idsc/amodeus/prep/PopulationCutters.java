/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.io.IOException;
import java.net.MalformedURLException;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;

public enum PopulationCutters {
    NETWORKBASED {
        @Override
        public void cut(Population population, Network network, ScenarioOptions scenarioOptions, Config config) throws MalformedURLException, IOException {
            new PopulationCutterNetworkBased(network).process(population);
        }
    },
    NONE {
        @Override
        public void cut(Population population, Network network, ScenarioOptions scenarioOptions, Config config) {
            // nothing to do here
        }
    };

    public abstract void cut(Population population, Network network, ScenarioOptions scenarioOptions, Config config) throws MalformedURLException, IOException, Exception;

}
