/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.testutils;

import java.io.File;
import java.net.URL;
import java.util.Objects;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.prep.ConfigCreator;
import ch.ethz.idsc.amodeus.prep.NetworkPreparer;
import ch.ethz.idsc.amodeus.prep.PopulationPreparer;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkPreparer;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.traveldata.TravelDataCreator;
import ch.ethz.idsc.amodeus.traveldata.TravelDataIO;
import ch.ethz.idsc.amodeus.util.io.ProvideAVConfig;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.matsim.av.config.AVConfig;
import ch.ethz.matsim.av.config.AVConfigReader;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.framework.AVConfigGroup;

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
        ScenarioOptions scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());

        // load Settings from IDSC Options
        File configFile = new File(workingDirectory, scenarioOptions.getPreparerConfigName());

        AVConfigGroup avCg = new AVConfigGroup();
        Config config = ConfigUtils.loadConfig(configFile.getAbsolutePath(), avCg);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        AVConfig avC = ProvideAVConfig.with(config, avCg);
        AVGeneratorConfig genConfig = avC.getOperatorConfigs().iterator().next().getGeneratorConfig();
        int numRt = (int) genConfig.getNumberOfVehicles();
        int endTime = (int) config.qsim().getEndTime();

        // 1) cut network (and reduce population to new network)
        networkPrepared = scenario.getNetwork();
        NetworkPreparer.run(networkPrepared, scenarioOptions);

        // 2) adapt the population to new network
        populationPrepared = scenario.getPopulation();
        PopulationPreparer.run(networkPrepared, populationPrepared, scenarioOptions, config, 10);

        // 3) create virtual Network
        VirtualNetworkPreparer virtualNetworkPreparer = VirtualNetworkPreparer.INSTANCE;
        VirtualNetwork<Link> virtualNetwork = virtualNetworkPreparer.create(networkPrepared, populationPrepared, scenarioOptions, numRt, endTime);

        // 4) create TravelData
        /** reading the customer requests */
        TravelData travelData = TravelDataCreator.create(virtualNetwork, networkPrepared, populationPrepared, scenarioOptions, numRt, endTime);
        File travelDataFile = new File(scenarioOptions.getVirtualNetworkName(), scenarioOptions.getTravelDataName());
        TravelDataIO.write(travelDataFile, travelData);

        // 5) save a simulation config file
        // IncludeActTypeOf.BaselineCH(config); // Only needed in Some Scenarios
        ConfigCreator.createSimulationConfigFile(config, scenarioOptions);
    }

    public Population getPreparedPopulation() {
        return populationPrepared;
    }

    public Network getPreparedNetwork() {
        return Objects.requireNonNull(networkPrepared);
    }


}
