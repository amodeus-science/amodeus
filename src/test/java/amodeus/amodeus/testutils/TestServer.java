/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.testutils;

import java.io.File;
import java.util.Objects;

import org.matsim.amodeus.AmodeusConfigurator;
import org.matsim.amodeus.config.AmodeusConfigGroup;
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

import amodeus.amodeus.analysis.Analysis;
import amodeus.amodeus.analysis.service.RequestHistoriesExportFromEvents;
import amodeus.amodeus.analysis.service.RoboTaxiHistoriesExportFromEvents;
import amodeus.amodeus.data.LocationSpec;
import amodeus.amodeus.data.ReferenceFrame;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.net.SimulationServer;
import amodeus.amodeus.options.ScenarioOptions;
import amodeus.amodeus.options.ScenarioOptionsBase;
import amodeus.amodeus.test.AnalysisTestExport;
import amodeus.amodeus.util.math.GlobalAssert;

public class TestServer {
    private final File workingDirectory;
    private final File configFile;
    private final AnalysisTestExport ate = new AnalysisTestExport();
    protected final ScenarioOptions scenarioOptions;

    public TestServer(File workingDirectory) throws Exception {
        this.workingDirectory = workingDirectory;
        System.out.println(workingDirectory);
        GlobalAssert.that(workingDirectory.isDirectory());
        scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        configFile = new File(scenarioOptions.getSimulationConfigName());
    }

    public void simulate() throws Exception {
        boolean waitForClients = scenarioOptions.getBoolean("waitForClients");
        StaticHelper.setup();

        LocationSpec locationSpec = scenarioOptions.getLocationSpec();

        ReferenceFrame referenceFrame = locationSpec.referenceFrame();

        // open server port for clients to connect to
        SimulationServer.INSTANCE.startAcceptingNonBlocking();
        SimulationServer.INSTANCE.setWaitForClients(waitForClients);

        // load MATSim configs - including av.xml where dispatcher is selected.
        System.out.println("loading config file " + configFile.getAbsoluteFile());

        GlobalAssert.that(configFile.exists()); // Test whether the config file
        // directory exists

        DvrpConfigGroup dvrpConfigGroup = new DvrpConfigGroup();
        dvrpConfigGroup.setTravelTimeEstimationAlpha(0.05);
        Config config = ConfigUtils.loadConfig(configFile.toString(), new AmodeusConfigGroup(), dvrpConfigGroup);
        config.planCalcScore().addActivityParams(new PlanCalcScoreConfigGroup.ActivityParams("activity"));

        config.qsim().setStartTime(0.0);
        config.qsim().setSimStarttimeInterpretation(StarttimeInterpretation.onlyUseStarttime);

        for (ActivityParams activityParams : config.planCalcScore().getActivityParams())
            // TODO @sebhoerl fix this to meaningful values, remove, or add comment
            // this was added because there are sometimes problems, is there a more elegant option?
            activityParams.setTypicalDuration(3600.0);

        String outputdirectory = config.controler().getOutputDirectory();
        System.out.println("outputdirectory = " + outputdirectory);

        // load scenario for simulation
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();
        Population population = scenario.getPopulation();
        GlobalAssert.that(Objects.nonNull(network) && Objects.nonNull(population));

        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, referenceFrame);
        Controler controller = new Controler(scenario);
        AmodeusConfigurator.configureController(controller, scenarioOptions);

        // run simulation
        controller.run();

        // close port for visualization
        SimulationServer.INSTANCE.stopAccepting();

        Analysis analysis = Analysis.setup(scenarioOptions, new File(workingDirectory, "output/001"), network, db);
        analysis.addAnalysisExport(ate);
        analysis.addAnalysisExport(new RoboTaxiHistoriesExportFromEvents(network, config));
        analysis.addAnalysisExport(new RequestHistoriesExportFromEvents(network, config));
        analysis.run();
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public AnalysisTestExport getAnalysisTestExport() {
        return ate;
    }

    public File getConfigFile() {
        return configFile;
    }
}
