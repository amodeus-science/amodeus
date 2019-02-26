/* amod - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.simulation;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.idsc.amodeus.data.LocationSpecDatabase;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.framework.AVConfigGroup;

/** The simulation Properties contains all the required values in the simulation. This helper calls enables a clean set up of the simulation in the server
 * class.
 * e.g.
 * - the three config files,
 * - The different directories
 * - The scenario with the population and the network */
public class SimulationProperties {
    private final ScenarioOptions scenarioOptions;
    public final File workingDirectory;
    public final String outputdirectory;
    public final File configFile;
    public final Config config;
    public Scenario scenario;
    public final Network network;
    public final Population population;
    public final MatsimAmodeusDatabase db;

    /** Loads the simulation properties without throwing an exception.
     * 
     * @return {@link SimulationProperties} */
    public static SimulationProperties load(File workingDirectory) {
        SimulationProperties simulationProperties = null;
        try {
            simulationProperties = new SimulationProperties(workingDirectory);
        } catch (IOException e) {
            System.err.println(
                    "Luckily we were able to stop the process before runing the simulation with incomplete settings. Who knows what would happen then... You are our last chance! Help me please I desire to run even more than you do.");
            e.printStackTrace();
            throw new RuntimeException();
        }
        return Objects.requireNonNull(simulationProperties);
    }

    /** To use this class the LocationSpecDatabase has to be set up in advance. This can be done with the Helper Class "Static" */
    protected SimulationProperties(File workingDirectory) throws IOException {
        GlobalAssert.that(!LocationSpecDatabase.INSTANCE.isEmpty());

        this.workingDirectory = workingDirectory;
        scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());

        // Locationspec needs to be set manually in Amodeus.properties
        GlobalAssert.that(!Objects.isNull(scenarioOptions.getLocationSpec()));

        // load MATSim configs - including av.xml where dispatcher is selected.
        configFile = new File(workingDirectory, scenarioOptions.getSimulationConfigName());
        System.out.println("loading config file " + configFile.getAbsoluteFile());
        GlobalAssert.that(configFile.exists()); // Test whether the config file directory exists

        DvrpConfigGroup dvrpConfigGroup = new DvrpConfigGroup();
        dvrpConfigGroup.setTravelTimeEstimationAlpha(0.05);
        config = ConfigUtils.loadConfig(configFile.toString(), new AVConfigGroup(), dvrpConfigGroup);
        outputdirectory = config.controler().getOutputDirectory();

        // load scenario for simulation
        scenario = ScenarioUtils.loadScenario(config);
        network = scenario.getNetwork();

        population = scenario.getPopulation();
        GlobalAssert.that(network != null && population != null);

        db = MatsimAmodeusDatabase.initialize(network, scenarioOptions.getLocationSpec().referenceFrame());
    }

    public ScenarioOptions getScenarioOptions() {
        return scenarioOptions;
    }
}
