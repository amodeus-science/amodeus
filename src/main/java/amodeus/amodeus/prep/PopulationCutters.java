/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.prep;

import java.io.IOException;
import java.net.MalformedURLException;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;

public enum PopulationCutters implements PopulationCutter {
    NETWORKBASED {
        @Override
        public void cut(Population population, Network network, Config config) throws MalformedURLException, IOException {
            new PopulationCutterNetworkBased(network, (int) config.qsim().getEndTime().seconds()).process(population);
        }
    },
    NONE {
        @Override
        public void cut(Population population, Network network, Config config) {
            // nothing to do here
        }
    };
}
