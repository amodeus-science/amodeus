/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.testutils;

import java.io.File;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

import com.google.inject.Key;
import com.google.inject.name.Names;

import ch.ethz.idsc.amodeus.analysis.Analysis;
import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusDispatcherModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusGeneratorModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusModule;
import ch.ethz.idsc.amodeus.net.DatabaseModule;
import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.net.SimulationServer;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.test.AnalysisTestExport;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.framework.AVConfigGroup;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVUtils;

public class TestServer {

    public static TestServer run() {
        return new TestServer();
    }

    private File workingDirectory;
    private ScenarioOptions scenarioOptions;
    private File configFile;
    private ReferenceFrame referenceFrame;
    private Config config;
    private String outputdirectory;
    private Scenario scenario;
    private Network network;
    private Population population;
    private Controler controler;
    private AnalysisTestExport ate;

    private TestServer() {

    }

    public TestServer on(File workingDirectory) throws Exception {
        this.workingDirectory = workingDirectory;
        System.out.println(workingDirectory);
        GlobalAssert.that(workingDirectory.isDirectory());
        scenarioOptions = ScenarioOptions.load(workingDirectory);
        simulate();
        return this;
    }

    private void simulate() throws Exception {
        boolean waitForClients = scenarioOptions.getBoolean("waitForClients");
        configFile = new File(workingDirectory, scenarioOptions.getSimulationConfigName());
        Static.setup();

        LocationSpec locationSpec = scenarioOptions.getLocationSpec();

        referenceFrame = locationSpec.referenceFrame();

        // open server port for clients to connect to
        SimulationServer.INSTANCE.startAcceptingNonBlocking();
        SimulationServer.INSTANCE.setWaitForClients(waitForClients);

        // load MATSim configs - including av.xml where dispatcher is selected.
        System.out.println("loading config file " + configFile.getAbsoluteFile());

        GlobalAssert.that(configFile.exists()); // Test whether the config file
        // directory exists

        DvrpConfigGroup dvrpConfigGroup = new DvrpConfigGroup();
        dvrpConfigGroup.setTravelTimeEstimationAlpha(0.05);
        config = ConfigUtils.loadConfig(configFile.toString(), new AVConfigGroup(), dvrpConfigGroup);
        config.planCalcScore().addActivityParams(new PlanCalcScoreConfigGroup.ActivityParams("activity"));

        for (ActivityParams activityParams : config.planCalcScore().getActivityParams()) {
            activityParams.setTypicalDuration(3600.0); // TODO fix this to meaningful values --> Sebastian how should we solve this?
        }

        outputdirectory = config.controler().getOutputDirectory();
        System.out.println("outputdirectory = " + outputdirectory);

        // load scenario for simulation
        scenario = ScenarioUtils.loadScenario(config);
        network = scenario.getNetwork();
        population = scenario.getPopulation();
        GlobalAssert.that(scenario != null && network != null && population != null);

        MatsimStaticDatabase.initializeSingletonInstance(network, referenceFrame);
        controler = new Controler(scenario);

        controler.addOverridingModule(new DvrpTravelTimeModule());
        controler.addOverridingModule(new AVModule());
        controler.addOverridingModule(new DatabaseModule());
        controler.addOverridingModule(new AmodeusGeneratorModule());
        controler.addOverridingModule(new AmodeusDispatcherModule());
        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                bind(Key.get(Network.class, Names.named("dvrp_routing"))).to(Network.class);
            }
        });
        controler.addOverridingModule(new AmodeusModule());

        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                AVUtils.registerDispatcherFactory(binder(), "DemoDispatcher", DemoDispatcher.Factory.class);
            }
        });

        // run simulation
        controler.run();

        // close port for visualization
        SimulationServer.INSTANCE.stopAccepting();

        Analysis analysis = Analysis.setup(workingDirectory, configFile, new File(workingDirectory, "output/001"), network);
        ate = new AnalysisTestExport();
        analysis.addAnalysisExport(ate);
        analysis.run();

    }

    public AnalysisTestExport getAnalysisTestExport() {
        return ate;

    }

    public File getConfigFile() {
        return configFile;
    }

    public ScenarioOptions getScenarioOptions() {
        return scenarioOptions;
    }
}
