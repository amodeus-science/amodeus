package org.matsim.amodeus.drt;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.drt.AmodeusDrtModule;
import org.matsim.amodeus.drt.AmodeusDrtQSimModule;
import org.matsim.amodeus.drt.MultiModeDrtModuleForAmodeus;
import org.matsim.amodeus.framework.AmodeusModule;
import org.matsim.amodeus.framework.VirtualNetworkModeModule;
import org.matsim.amodeus.scenario.TestScenarioGenerator;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.routing.DrtRoute;
import org.matsim.contrib.drt.routing.DrtRouteFactory;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.drt.run.DrtConfigGroup.OperationalScheme;
import org.matsim.contrib.drt.run.DrtConfigs;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtModule;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.FleetWriter;
import org.matsim.contrib.dvrp.fleet.ImmutableDvrpVehicleSpecification;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.run.DvrpQSimComponents;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlansConfigGroup.HandlingOfPlansWithoutRoutingMode;
import org.matsim.core.config.groups.QSimConfigGroup.StarttimeInterpretation;
import org.matsim.core.controler.Controler;

public class RunDrtTest {
    private final static String DRT_MODE = "av";

    @BeforeClass
    public static void doYourOneTimeSetup() {
        new File("test_output").mkdir();
        new File("test_data").mkdir();
    }

    @AfterClass
    public static void doYourOneTimeTeardown() throws IOException {
        FileUtils.deleteDirectory(new File("test_output"));
        FileUtils.deleteDirectory(new File("test_data"));
    }

    @Test
    public void testDefaultDrt() {
        Config config = ConfigUtils.createConfig(new MultiModeDrtConfigGroup(), new DvrpConfigGroup());
        Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);
        run(config, scenario, false);
    }

    @Test
    public void testAmodeusDrt() {
        Config config = ConfigUtils.createConfig(new MultiModeDrtConfigGroup(), new DvrpConfigGroup(), new AmodeusConfigGroup());
        Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);
        run(config, scenario, true);

        // Currently, Amodeus does not know where to put the auto-generated ScenarioOptions file.
        // We can make this a config option for AmodeusConfigGroup.
        FileUtils.deleteQuietly(new File("AmodeusOptions.properties"));
    }

    static public void run(Config config, Scenario scenario, boolean useAmodeus) {
        // CONFIG PART

        // Set up MATSim configuration to be compatible with DRT
        config.plans().setHandlingOfPlansWithoutRoutingMode(HandlingOfPlansWithoutRoutingMode.useMainModeIdentifier);
        config.qsim().setStartTime(0.0);
        config.qsim().setSimStarttimeInterpretation(StarttimeInterpretation.onlyUseStarttime);

        config.qsim().setNumberOfThreads(1);

        // Set up missing scoring parameters
        config.planCalcScore().getOrCreateModeParams(DRT_MODE);

        // Set up DRT mode
        DrtConfigGroup drtModeConfig = new DrtConfigGroup();
        drtModeConfig.setMode(DRT_MODE);

        drtModeConfig.setMaxTravelTimeBeta(600.0);
        drtModeConfig.setMaxTravelTimeAlpha(1.4);
        drtModeConfig.setMaxWaitTime(600.0);
        drtModeConfig.setStopDuration(60);
        drtModeConfig.setRejectRequestIfMaxWaitOrTravelTimeViolated(true);
        drtModeConfig.setOperationalScheme(OperationalScheme.door2door);

        MultiModeDrtConfigGroup drtConfig = MultiModeDrtConfigGroup.get(config);
        drtConfig.addParameterSet(drtModeConfig);
        DrtConfigs.adjustDrtConfig(drtModeConfig, config.planCalcScore(), config.plansCalcRoute());

        // Create a fleet on the fly
        String vehiclesFile = new File("test_data/drt_vehicles.xml.gz").getAbsolutePath();
        drtModeConfig.setVehiclesFile(vehiclesFile);
        createFleet(vehiclesFile, 100, scenario.getNetwork());

        // Set up DVRP
        DvrpConfigGroup dvrpConfig = DvrpConfigGroup.get(config);
        dvrpConfig.setTravelTimeEstimationAlpha(1.0);
        dvrpConfig.setTravelTimeEstimationBeta(900);

        // SCENARIO PART
        scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory(DrtRoute.class, new DrtRouteFactory());

        // CONTROLLER PART
        Controler controller = new Controler(scenario);

        // Add DVRP and activate modes
        controller.addOverridingModule(new DvrpModule());
        controller.configureQSimComponents(DvrpQSimComponents.activateModes(drtModeConfig.getMode()));

        if (!useAmodeus) {
            // No Amodeus, so we use standard MultiModeDrtModule
            controller.addOverridingModule(new MultiModeDrtModule());
        } else {
            // Add DRT, but NOT with MultiModeDrtModule, but with MultiModeDrtModuleForAmodeus
            // because right now we remove DRT's analysis components as they are not compatible yet
            controller.addOverridingModule(new MultiModeDrtModuleForAmodeus());
        }

        // Here we start overriding things of DRT with Amodeus
        if (useAmodeus) {

            // This is a per-mode config, which usually is contained in a AmodeusConfigGroup,
            // here we only use it to set up a small portion of Amodeus (the dispatching part),
            // and not scoring, waiting times, etc.
            AmodeusModeConfig amodeusModeConfig = new AmodeusModeConfig(drtModeConfig.getMode());

            // We can choose the dispatcher and set additional options. Note that some dispatchers
            // rely heavily on GLPK. You need to install it and then tell JAVA where to find it
            // via -Djava.library.path=/path/to/glpk/lib/jni on the command line.
            amodeusModeConfig.getDispatcherConfig().setType("FeedforwardFluidicRebalancingPolicy");

            // Change, for instance, to "GlobalBipartiteMatchingDispatcher" if you want to
            // test without GLPK!

            // Disable Amodeus-specific output (e.g., for the viewer)
            amodeusModeConfig.getDispatcherConfig().setPublishPeriod(0);

            // Path where to generate or read a VirtualNetwork and TravelData for rebalancing.
            // Note that not all dispatchers need this.
            amodeusModeConfig.getDispatcherConfig().setVirtualNetworkPath(new File("test_data/virtualNetwork").getAbsolutePath());
            amodeusModeConfig.getDispatcherConfig().setTravelDataPath(new File("test_data/travelData").getAbsolutePath());

            // Add a subset of Amodeus modules which usually would be added automatically
            // in the upper-level AmodeusModule.
            controller.addOverridingModule(new VirtualNetworkModeModule(amodeusModeConfig));
            controller.addOverridingModule(new AmodeusModule());

            // Add overriding modules for the Drt <-> Amodeus integration, which override some
            // components of DRT. Later on, we would only override DrtOptimizer, but we are
            // not there yet, because Amodeus internally still works with AmodeusStayTask, etc.
            // and does not understand DrtStayTask, etc.
            controller.addOverridingModule(new AmodeusDrtModule(amodeusModeConfig));
            controller.addOverridingQSimModule(new AmodeusDrtQSimModule(drtModeConfig.getMode()));
        }

        controller.run();
    }

    static public void createFleet(String path, int numberOfVehicles, Network network) {
        Random random = new Random(0);

        List<Link> links = network.getLinks().values().stream().filter(link -> link.getAllowedModes().contains("car")).collect(Collectors.toList());

        new FleetWriter(IntStream.range(0, 100).mapToObj(i -> {
            return ImmutableDvrpVehicleSpecification.newBuilder() //
                    .id(Id.create("drt" + i, DvrpVehicle.class)) //
                    .startLinkId(links.get(random.nextInt(links.size())).getId()) //
                    .capacity(4) //
                    .serviceBeginTime(0.0) //
                    .serviceEndTime(30.0 * 3600.0) //
                    .build();
        })).write(path);
    }
}
