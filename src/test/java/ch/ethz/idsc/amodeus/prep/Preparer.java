package ch.ethz.idsc.amodeus.prep;

import java.io.File;
import java.io.IOException;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;

/* package */ class Preparer {

    /* package */ final ScenarioOptions scenOpt;
    /* package */ final Network network;
    /* package */ final Population population;
    /* package */ final Config config;

    public Preparer() throws IOException {
        // Static.setup();
        File workingDirectory = MultiFileTools.getWorkingDirectory();

        /** amodeus options */
        scenOpt = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());

        /** MATSim config */
        config = ConfigUtils.loadConfig(scenOpt.getPreparerConfigName());
        Scenario scenario = ScenarioUtils.loadScenario(config);

        /** adaption of MATSim network, e.g., radius cutting */
        Network network = scenario.getNetwork();
        this.network = NetworkPreparer.run(network, scenOpt);

        /** adaption of MATSim population, e.g., radius cutting */
        this.population = scenario.getPopulation();
    }
}
