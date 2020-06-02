/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.testutils;

import java.io.File;
import java.util.Collections;
import java.util.Objects;

import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.amodeus.config.modal.GeneratorConfig;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.scenario.ScenarioUtils;

import amodeus.amodeus.options.ScenarioOptions;
import amodeus.amodeus.options.ScenarioOptionsBase;
import amodeus.amodeus.prep.ConfigCreator;
import amodeus.amodeus.prep.NetworkPreparer;
import amodeus.amodeus.prep.PopulationPreparer;
import amodeus.amodeus.prep.VirtualNetworkPreparer;
import amodeus.amodeus.traveldata.StaticTravelData;
import amodeus.amodeus.traveldata.StaticTravelDataCreator;
import amodeus.amodeus.traveldata.TravelDataIO;
import amodeus.amodeus.util.matsim.SnapToClosestNetworkLink;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;

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
        File configFile = new File(scenarioOptions.getPreparerConfigName());

        AmodeusConfigGroup avConfigGroup = new AmodeusConfigGroup();
        Config config = ConfigUtils.loadConfig(configFile.getAbsolutePath(), avConfigGroup);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        GeneratorConfig avGeneratorConfig = //
                avConfigGroup.getModes().values().iterator().next().getGeneratorConfig();
        int numRt = avGeneratorConfig.getNumberOfVehicles();
        int endTime = (int) config.qsim().getEndTime().seconds();

        // 1) cut network (and reduce population to new network)
        networkPrepared = scenario.getNetwork();        
        NetworkPreparer.run(networkPrepared, scenarioOptions);

        // 2) adapt the population to new network
        populationPrepared = scenario.getPopulation();

        // To make Test input data consistent (e.g. avoid people departing fom "tram" links)
        SnapToClosestNetworkLink.run(populationPrepared, networkPrepared, TransportMode.car);

        PopulationPreparer.run(networkPrepared, populationPrepared, scenarioOptions, config, 10);

        // 3) create virtual Network
        
        // Amodeus uses internally a mode-filtered network (default is the car network). The provided
        // VirtualNetwork needs to be consistent with this node-filtered network.
        Network roadNetwork = NetworkUtils.createNetwork();
        new TransportModeNetworkFilter(networkPrepared).filter(roadNetwork, Collections.singleton("car"));
        new NetworkCleaner().run(roadNetwork);
        
        VirtualNetworkPreparer virtualNetworkPreparer = VirtualNetworkPreparer.INSTANCE;
        VirtualNetwork<Link> virtualNetwork = //
                virtualNetworkPreparer.create(roadNetwork, populationPrepared, scenarioOptions, numRt, endTime);

        // 4) create TravelData
        /** reading the customer requests */
        StaticTravelData travelData = StaticTravelDataCreator.create( //
                scenarioOptions.getWorkingDirectory(), //
                virtualNetwork, roadNetwork, populationPrepared, //
                scenarioOptions.getdtTravelData(), numRt, endTime);
        File travelDataFile = new File(scenarioOptions.getVirtualNetworkDirectoryName(), scenarioOptions.getTravelDataName());
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
