/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.testutils;

import java.io.File;
import java.util.Objects;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.prep.ConfigCreator;
import ch.ethz.idsc.amodeus.prep.NetworkPreparer;
import ch.ethz.idsc.amodeus.prep.PopulationPreparer;
import ch.ethz.idsc.amodeus.prep.VirtualNetworkPreparer;
import ch.ethz.idsc.amodeus.traveldata.StaticTravelData;
import ch.ethz.idsc.amodeus.traveldata.StaticTravelDataCreator;
import ch.ethz.idsc.amodeus.traveldata.TravelDataIO;
import ch.ethz.idsc.amodeus.util.io.ProvideAVConfig;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.matsim.av.config.AVConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.framework.AVConfigGroup;

public class TestPreparer {

    public static TestPreparer run(File workingDirectory) throws Exception {
        return new TestPreparer(workingDirectory);
    }

    // ---
    private final Network networkPrepared;
    private final Population populationPrepared;

    private TestPreparer(File workingDirectory) throws Exception {
        System.out.println("working directory: " + workingDirectory);

        // run preparer in simulation working directory
        ScenarioOptions scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());

        // load Settings from IDSC Options
        File configFile = new File(workingDirectory, scenarioOptions.getPreparerConfigName());

        AVConfigGroup avConfigGroup = new AVConfigGroup();
        Config config = ConfigUtils.loadConfig(configFile.getAbsolutePath(), avConfigGroup);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        AVConfig avConfig = ProvideAVConfig.with(config, avConfigGroup);
        AVGeneratorConfig avGeneratorConfig = //
                avConfig.getOperatorConfigs().iterator().next().getGeneratorConfig();
        int numRt = (int) avGeneratorConfig.getNumberOfVehicles();
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
        StaticTravelData travelData = StaticTravelDataCreator.create( //
                scenarioOptions.getWorkingDirectory(), //
                virtualNetwork, networkPrepared, populationPrepared, //
                scenarioOptions.getdtTravelData(), numRt, endTime);
        File travelDataFile = new File(scenarioOptions.getVirtualNetworkName(), scenarioOptions.getTravelDataName());
        TravelDataIO.writeStatic(travelDataFile, travelData);

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
