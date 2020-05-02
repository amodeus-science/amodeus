/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.testutils;

import java.io.File;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.QSimConfigGroup.StarttimeInterpretation;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.idsc.amodeus.analysis.Analysis;
import ch.ethz.idsc.amodeus.analysis.service.RequestHistoriesExportFromEvents;
import ch.ethz.idsc.amodeus.analysis.service.RoboTaxiHistoriesExportFromEvents;
import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.SimulationServer;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.test.AnalysisTestExport;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.refactoring.AmodeusConfigurator;

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
    private Controler controller;
    private AnalysisTestExport ate;

    private TestServer() {
        // ---
    }

    public TestServer on(File workingDirectory) throws Exception {
        this.workingDirectory = workingDirectory;
        System.out.println(workingDirectory);
        GlobalAssert.that(workingDirectory.isDirectory());
        scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        simulate();
        return this;
    }

    private void simulate() throws Exception {
        boolean waitForClients = scenarioOptions.getBoolean("waitForClients");
        configFile = new File(scenarioOptions.getSimulationConfigName());
        StaticHelper.setup();

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

        config.qsim().setStartTime(0.0);
        config.qsim().setSimStarttimeInterpretation(StarttimeInterpretation.onlyUseStarttime);

        for (ActivityParams activityParams : config.planCalcScore().getActivityParams())
            // TODO @sebhoerl fix this to meaningful values, remove, or add comment
            // this was added because there are sometimes problems, is there a more elegant option?
            activityParams.setTypicalDuration(3600.0);

        outputdirectory = config.controler().getOutputDirectory();
        System.out.println("outputdirectory = " + outputdirectory);

        // load scenario for simulation
        scenario = ScenarioUtils.loadScenario(config);
        network = scenario.getNetwork();
        population = scenario.getPopulation();
        GlobalAssert.that(scenario != null && network != null && population != null);

        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, referenceFrame);
        controller = new Controler(scenario);
        AmodeusConfigurator.configureController(controller, db, scenarioOptions);

        // run simulation
        controller.run();

        // close port for visualization
        SimulationServer.INSTANCE.stopAccepting();

        Analysis analysis = Analysis.setup(scenarioOptions, new File(workingDirectory, "output/001"), network, db);
        ate = new AnalysisTestExport();
        analysis.addAnalysisExport(ate);
        analysis.addAnalysisExport(new RoboTaxiHistoriesExportFromEvents(network, config));
        analysis.addAnalysisExport(new RequestHistoriesExportFromEvents(network, config));
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
