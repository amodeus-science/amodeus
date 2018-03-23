/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.testutils;

import java.io.File;
import java.util.Objects;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.prep.ConfigCreator;
import ch.ethz.idsc.amodeus.prep.NetworkPreparer;
import ch.ethz.idsc.amodeus.prep.PopulationPreparer;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkPreparer;

public class TestPreparer {

    public static TestPreparer run() {
        return new TestPreparer();
    }

    private Network networkPrepared;
    private Population populationPrepared;

    private TestPreparer() {

    }

    public TestPreparer on(File workingDirectory) throws Exception {
        prepare(workingDirectory);
        return this;
    }

    private void prepare(File workingDirectory) throws Exception {
        System.out.println("working directory: " + workingDirectory);

        // run preparer in simulation working directory
        ScenarioOptions scenarioOptions = ScenarioOptions.load(workingDirectory);

        // load Settings from IDSC Options
        File configFile = new File(workingDirectory, scenarioOptions.getPreparerConfigName());
        Config config = ConfigUtils.loadConfig(configFile.getAbsolutePath());

        Scenario scenario = ScenarioUtils.loadScenario(config);

        // 1) cut network (and reduce population to new network)
        networkPrepared = scenario.getNetwork();
        NetworkPreparer.run(networkPrepared, scenarioOptions, workingDirectory);

        // 2) adapt the population to new network
        populationPrepared = scenario.getPopulation();
        PopulationPreparer.run(networkPrepared, populationPrepared, scenarioOptions, config, workingDirectory);

        // 3) create virtual Network
        VirtualNetworkPreparer.run(networkPrepared, populationPrepared, scenarioOptions, workingDirectory);

        // 4) save a simulation config file
        // IncludeActTypeOf.BaselineCH(config); // Only needed in Some Scenarios
        ConfigCreator.createSimulationConfigFile(config, scenarioOptions, workingDirectory);

    }

    public Population getPreparedPopulation() {
        return populationPrepared;
    }

    public Network getPreparedNetwork() {
        return Objects.requireNonNull(networkPrepared);
    }

}
