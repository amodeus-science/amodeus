/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.population.PopulationUtils;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusDatabaseModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusDispatcherModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusVehicleGeneratorModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusVehicleToVSGeneratorModule;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.LPOptions;
import ch.ethz.idsc.amodeus.options.LPOptionsBase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.prep.MatsimKMeansVirtualNetworkCreator;
import ch.ethz.idsc.amodeus.test.TestFileHandling;
import ch.ethz.idsc.amodeus.testutils.TestUtils;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.traveldata.TravelDataCreator;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.matsim.av.config.AVConfig;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.config.AVOperatorConfig;
import ch.ethz.matsim.av.framework.AVConfigGroup;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.scenario.TestScenarioAnalyzer;
import ch.ethz.matsim.av.scenario.TestScenarioGenerator;

@RunWith(Parameterized.class)
public class StandardMATSimScenarioTest {
    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        // SingleHeuristic is added as a reference case, to check that the av package is
        // working properly

        // ATTENTION: DriveByDispatcher is not tested, because of long runtime.
        return Arrays.asList(new Object[][] { { "SingleHeuristic" }, { "DemandSupplyBalancingDispatcher" }, { "GlobalBipartiteMatchingDispatcher" },
                // { "AdaptiveRealTimeRebalancingPolicy" }, // TODO TEST @Sebastian, is the input data correct? LP fails sometimes, (depening on order)
                { "FeedforwardFluidicRebalancingPolicy" } });

    }

    final private String dispatcher;

    public StandardMATSimScenarioTest(String dispatcher) {
        this.dispatcher = dispatcher;
    }

    private static void makeMultimodal(Scenario scenario) {
        // Add pt-links to the network to test a multimodal network as it appears in standard MATSim use cases

        Network network = scenario.getNetwork();
        NetworkFactory factory = network.getFactory();

        // Let's build a fast track through the scenario
        for (int i = 0; i < 9; i++) {
            Id<Link> ptFowardLinkId = Id.createLinkId(String.format("pt_fwd_%d:%d", i, i));
            Id<Link> ptBackwardLinkId = Id.createLinkId(String.format("pt_bck_%d:%d", i, i));
            Id<Node> fromNodeId = Id.createNodeId(String.format("%d:%d", i, i));
            Id<Node> toNodeId = Id.createNodeId(String.format("%d:%d", i + 1, i + 1));

            Link ptFowardLink = factory.createLink(ptFowardLinkId, network.getNodes().get(fromNodeId), network.getNodes().get(toNodeId));
            ptFowardLink.setFreespeed(100.0 * 1000.0 / 3600.0);
            ptFowardLink.setLength(1000.0);
            ptFowardLink.setAllowedModes(Collections.singleton("pt"));
            network.addLink(ptFowardLink);

            Link ptBackwardLink = factory.createLink(ptBackwardLinkId, network.getNodes().get(toNodeId), network.getNodes().get(fromNodeId));
            ptBackwardLink.setFreespeed(100.0 * 1000.0 / 3600.0);
            ptBackwardLink.setLength(1000.0);
            ptBackwardLink.setAllowedModes(Collections.singleton("pt"));
            network.addLink(ptBackwardLink);
        }

        // Also, a routed population may have "pt interaction" activities, which take place at links that are not part of the road network. Amodeus must be able
        // to
        // handle these cases.

        for (Person person : scenario.getPopulation().getPersons().values()) {
            for (Plan plan : person.getPlans()) {
                Activity trickyActivity = PopulationUtils.createActivityFromCoordAndLinkId("pt interaction", new Coord(5500.0, 5500.0), Id.createLinkId("pt_fwd_5:5"));

                plan.getPlanElements().add(PopulationUtils.createLeg("walk"));
                plan.getPlanElements().add(trickyActivity);
            }
        }
    }

    private static void fixInvalidActivityLocations(Network network, Population population) {
        // In the test fixture there are agents who start and end activities on non-car links. This should not be happen and is fixed here.

        Network roadNetwork = NetworkUtils.createNetwork();
        new TransportModeNetworkFilter(network).filter(roadNetwork, Collections.singleton("car"));

        for (Person person : population.getPersons().values()) {
            for (Plan plan : person.getPlans()) {
                for (PlanElement element : plan.getPlanElements()) {
                    if (element instanceof Activity) {
                        Activity activity = (Activity) element;

                        Link link = network.getLinks().get(activity.getLinkId());

                        if (!link.getAllowedModes().contains("car")) {
                            link = NetworkUtils.getNearestLink(roadNetwork, link.getCoord());
                            activity.setLinkId(link.getId());
                        }
                    }
                }
            }
        }
    }

    @BeforeClass
    public static void setUp() throws IOException {
        // copy scenario data into main directory
        File scenarioDirectory = new File(TestUtils.getSuperFolder("amodeus"), "resources/testScenario");
        File workingDirectory = MultiFileTools.getWorkingDirectory();
        GlobalAssert.that(workingDirectory.exists());
        TestFileHandling.copyScnearioToMainDirectory(scenarioDirectory.getAbsolutePath(), workingDirectory.getAbsolutePath());
    }

    @Test
    public void testStandardMATSimScenario() throws IOException {
        /* This test runs a small test scenario with the different dispatchers and makes
         * sure that all 100 generated agents arrive */
        StaticHelper.setup();
        MatsimRandom.reset();

        // Set up
        Config config = ConfigUtils.createConfig(new AVConfigGroup(), new DvrpConfigGroup());
        Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);

        File workingDirectory = MultiFileTools.getWorkingDirectory();
        ScenarioOptions simOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        LocationSpec locationSpec = simOptions.getLocationSpec();
        ReferenceFrame referenceFrame = locationSpec.referenceFrame();
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(scenario.getNetwork(), referenceFrame);

        PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore().getOrCreateModeParams(AVModule.AV_MODE);
        modeParams.setMonetaryDistanceRate(0.0);
        modeParams.setMarginalUtilityOfTraveling(8.86);
        modeParams.setConstant(0.0);

        Controler controler = new Controler(scenario);
        controler.addOverridingModule(new DvrpTravelTimeModule());
        controler.addOverridingModule(new AVModule());
        controler.addOverridingModule(new AmodeusModule());
        controler.addOverridingModule(new AmodeusDispatcherModule());
        controler.addOverridingModule(new AmodeusVehicleGeneratorModule());
        controler.addOverridingModule(new AmodeusVehicleToVSGeneratorModule());
        controler.addOverridingModule(new AmodeusDatabaseModule(db));

        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                // ---
            }

            @Provides
            @Singleton
            @Named(DvrpModule.DVRP_ROUTING)
            public Network provideAVNetwork(Network fullNetwork) {
                /* TODO TEST Eventually, this should go directly into the AmodeusModule.
                 * - For backward compatibility AmodeusModule provides a FULL network, see there.
                 * - However, here we want a "clean" test case where only a sub-network is used,
                 * i.e. in this case all links with the "car" mode. */

                TransportModeNetworkFilter filter = new TransportModeNetworkFilter(fullNetwork);

                Network filtered = NetworkUtils.createNetwork();
                filter.filter(filtered, Collections.singleton(TransportMode.car));

                return filtered;
            }
        });

        // Make the scenario multimodal
        fixInvalidActivityLocations(scenario.getNetwork(), scenario.getPopulation());
        makeMultimodal(scenario);

        // Config

        AVConfig avConfig = new AVConfig();
        AVOperatorConfig operatorConfig = avConfig.createOperatorConfig("test");
        AVGeneratorConfig generatorConfig = operatorConfig.createGeneratorConfig("VehicleToVSGenerator");
        generatorConfig.setNumberOfVehicles(100);
        int endTime = (int) config.qsim().getEndTime();

        // Choose a dispatcher
        AVDispatcherConfig dispatcherConfig = operatorConfig.createDispatcherConfig(dispatcher);

        // Make sure that we do not need the SimulationObjectCompiler
        dispatcherConfig.addParam("publishPeriod", "-1");

        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                bind(AVConfig.class).toInstance(avConfig);
            }
        });

        // Set up a virtual network for the LPFBDispatcher

        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                // ---
            }

            @Provides
            @Singleton
            public VirtualNetwork<Link> provideVirtualNetwork(@Named(AVModule.AV_MODE) Network network) {
                // Since we have no virtual netowrk saved in the working directory for our test
                // sceanario, we need to provide a custom one for the LPFB dispatcher

                return MatsimKMeansVirtualNetworkCreator.createVirtualNetwork(scenario.getPopulation(), network, 2, true);
            }

            @Provides
            @Singleton
            public TravelData provideTravelData(VirtualNetwork<Link> virtualNetwork, @Named(AVModule.AV_MODE) Network network, Population population) throws Exception {
                // Same as for the virtual network: For the LPFF dispatcher we need travel
                // data, which we generate on the fly here.
                ScenarioOptions scenarioOptions = new ScenarioOptions(MultiFileTools.getWorkingDirectory(), ScenarioOptionsBase.getDefault());

                LPOptions lpOptions = new LPOptions(MultiFileTools.getWorkingDirectory(), LPOptionsBase.getDefault());
                lpOptions.setProperty(LPOptionsBase.LPSOLVER, "timeInvariant");
                lpOptions.saveAndOverwriteLPOptions();
                TravelData travelData = TravelDataCreator.create(virtualNetwork, network, population, scenarioOptions.getdtTravelData(), (int) generatorConfig.getNumberOfVehicles(), endTime);
                return travelData;
            }
        });

        // Set up test analyzer and run

        TestScenarioAnalyzer analyzer = new TestScenarioAnalyzer();
        controler.addOverridingModule(analyzer);

        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                addEventHandlerBinding().toInstance(new LinkEnterEventHandler() {
                    @Override
                    public void handleEvent(LinkEnterEvent event) {
                        // Fail if an AV attempts to enter a pt link

                        if (event.getVehicleId().toString().startsWith("av_") && event.getLinkId().toString().startsWith("pt")) {
                            Assert.fail("AV attempted to enter PT link");
                        }
                    }
                });
            }
        });

        controler.run();
        Assert.assertEquals(0, analyzer.numberOfDepartures - analyzer.numberOfArrivals);
    }

    @AfterClass
    public static void tearDownOnce() throws IOException {
        TestFileHandling.removeGeneratedFiles();
    }
}
