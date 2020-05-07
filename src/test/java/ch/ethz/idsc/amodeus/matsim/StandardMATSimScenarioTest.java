/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
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
import org.matsim.contrib.dvrp.run.DvrpModes;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;

import com.google.inject.TypeLiteral;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.LPOptions;
import ch.ethz.idsc.amodeus.options.LPOptionsBase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.parking.AmodeusParkingModule;
import ch.ethz.idsc.amodeus.parking.ParkingCapacityGenerators;
import ch.ethz.idsc.amodeus.parking.strategies.ParkingStrategies;
import ch.ethz.idsc.amodeus.prep.MatsimKMeansVirtualNetworkCreator;
import ch.ethz.idsc.amodeus.test.TestFileHandling;
import ch.ethz.idsc.amodeus.traveldata.StaticTravelDataCreator;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.DispatcherConfig;
import ch.ethz.matsim.av.config.operator.GeneratorConfig;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.scenario.TestScenarioAnalyzer;
import ch.ethz.matsim.av.scenario.TestScenarioGenerator;
import ch.ethz.refactoring.AmodeusConfigurator;

@RunWith(Parameterized.class)
public class StandardMATSimScenarioTest {
    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        // SingleHeuristic is added as a reference case, to check that the av package is
        // working properly

        // ATTENTION: DriveByDispatcher is not tested, because of long runtime.
        return Arrays.asList(new Object[][] { //
                { "SingleHeuristic" }, //
                { "DemandSupplyBalancingDispatcher" }, //
                { "GlobalBipartiteMatchingDispatcher" }, //
                { "FeedforwardFluidicRebalancingPolicy" }, //
                { "AdaptiveRealTimeRebalancingPolicy" }, //
                { "ExtDemandSupplyBeamSharing" }, //
                { "TShareDispatcher" }, //
                { "FirstComeFirstServedStrategy" }, //
                { "DynamicRideSharingStrategy" }, //
                { "RestrictedLinkCapacityDispatcher" }, //
                { "ModelFreeAdaptiveRepositioning" }, //
                { "DFRStrategy" }, //
                { "NoExplicitCommunication" }, //
                { "SBNoExplicitCommunication" }, //

                // This one doesn't finish all requests. Bug or not enough of time? Also it's not good in an automated unit test because it
                // produces large amounts of log output.
                // { "HighCapacityDispatcher" },

                // Also has not enough of time to finish all requests
                // { "NorthPoleSharedDispatcher" },
        });
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

        /* for (Person person : scenario.getPopulation().getPersons().values())
         * for (Plan plan : person.getPlans()) {
         * Activity trickyActivity = PopulationUtils.createActivityFromCoordAndLinkId("pt interaction", new Coord(5500.0, 5500.0), Id.createLinkId("pt_fwd_5:5"));
         * 
         * plan.getPlanElements().add(PopulationUtils.createLeg("walk"));
         * plan.getPlanElements().add(trickyActivity);
         * } */

        // TODO @sebhoerl Difficult to keep this in as handling of "interaction" activities become much smarter in MATSim now. We would need to
        // set up a much more realistic test scenario. There is one in the AV package, so we can use that one!
    }

    private static void fixInvalidActivityLocations(Network network, Population population) {
        // In the test fixture there are agents who start and end activities on non-car links. This should not be happen and is fixed here.

        Network roadNetwork = NetworkUtils.createNetwork();
        new TransportModeNetworkFilter(network).filter(roadNetwork, Collections.singleton("car"));

        for (Person person : population.getPersons().values())
            for (Plan plan : person.getPlans())
                for (PlanElement element : plan.getPlanElements())
                    if (element instanceof Activity) {
                        Activity activity = (Activity) element;

                        Link link = network.getLinks().get(activity.getLinkId());

                        if (!link.getAllowedModes().contains("car")) {
                            link = NetworkUtils.getNearestLink(roadNetwork, link.getCoord());
                            activity.setLinkId(link.getId());
                        }
                    }
    }

    @BeforeClass
    public static void setUp() throws IOException {
        // copy scenario data into main directory
        File scenarioDirectory = new File(Locate.repoFolder(StandardMATSimScenarioTest.class, "amodeus"), "resources/testScenario");
        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        GlobalAssert.that(workingDirectory.isDirectory());
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

        File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
        ScenarioOptions simOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        LocationSpec locationSpec = simOptions.getLocationSpec();
        ReferenceFrame referenceFrame = locationSpec.referenceFrame();
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(scenario.getNetwork(), referenceFrame);

        PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore().getOrCreateModeParams("av");
        modeParams.setMonetaryDistanceRate(0.0);
        modeParams.setMarginalUtilityOfTraveling(8.86);
        modeParams.setConstant(0.0);

        int i = new Random().nextInt(ParkingStrategies.values().length);
        simOptions.setProperty("parkingStrategy", ParkingStrategies.values()[i].name());
        int j = new Random().nextInt(ParkingStrategies.values().length);
        simOptions.setProperty("parkingCapacityGenerator", ParkingCapacityGenerators.values()[j].name());

        Controler controller = new Controler(scenario);
        AmodeusConfigurator.configureController(controller, db, simOptions);
        controller.addOverridingModule(new AmodeusParkingModule(simOptions, new Random()));

        // Make the scenario multimodal
        fixInvalidActivityLocations(scenario.getNetwork(), scenario.getPopulation());
        makeMultimodal(scenario);

        // Config
        AVConfigGroup avConfig = AVConfigGroup.getOrCreate(config);
        avConfig.setAllowedLinkMode("car");

        OperatorConfig operatorConfig = new OperatorConfig();
        operatorConfig.setId(AVOperator.createId("test"));
        avConfig.addOperator(operatorConfig);

        GeneratorConfig generatorConfig = operatorConfig.getGeneratorConfig();
        generatorConfig.setType("VehicleToVSGenerator");
        generatorConfig.setNumberOfVehicles(50);

        int endTime = (int) config.qsim().getEndTime().seconds();

        // Choose a dispatcher
        DispatcherConfig dispatcherConfig = operatorConfig.getDispatcherConfig();
        dispatcherConfig.addParam("DFR", "true");
        dispatcherConfig.addParam("infoLinePeriod", "3600");
        dispatcherConfig.setType(dispatcher);

        // Make sure that we do not need the SimulationObjectCompiler
        dispatcherConfig.addParam("publishPeriod", "-1");

        // Set up a virtual network for the LPFBDispatcher

        controller.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                // TODO: This is not modalized now!

                bind(DvrpModes.key(new TypeLiteral<VirtualNetwork<Link>>() {
                }, "av")).toProvider(ModalProviders.createProvider("av", getter -> {
                    Network network = getter.getModal(Network.class);
                    return MatsimKMeansVirtualNetworkCreator.createVirtualNetwork(scenario.getPopulation(), network, 2, true);
                }));

                bind(DvrpModes.key(new TypeLiteral<TravelData>() {
                }, "av")).toProvider(ModalProviders.createProvider("av", getter -> {
                    try {
                        LPOptions lpOptions = new LPOptions(simOptions.getWorkingDirectory(), LPOptionsBase.getDefault());
                        lpOptions.setProperty(LPOptionsBase.LPSOLVER, "timeInvariant");
                        lpOptions.saveAndOverwriteLPOptions();

                        VirtualNetwork<Link> virtualNetwork = getter.getModal(new TypeLiteral<VirtualNetwork<Link>>() {
                        });
                        Network network = getter.getModal(Network.class);
                        Population population = getter.get(Population.class);

                        return StaticTravelDataCreator.create(simOptions.getWorkingDirectory(), virtualNetwork, network, population, simOptions.getdtTravelData(),
                                generatorConfig.getNumberOfVehicles(), endTime);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
            }
        });

        // Set up test analyzer and run

        TestScenarioAnalyzer analyzer = new TestScenarioAnalyzer();
        controller.addOverridingModule(analyzer);

        controller.addOverridingModule(new AbstractModule() {
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

        controller.run();

        if (analyzer.numberOfDepartures != analyzer.numberOfArrivals) {
            System.out.println("numberOfDepartures=" + analyzer.numberOfDepartures);
            System.out.println("numberOfArrivals  =" + analyzer.numberOfArrivals);
        }

        Assert.assertEquals(analyzer.numberOfDepartures, analyzer.numberOfArrivals);
    }

    @AfterClass
    public static void tearDownOnce() throws IOException {
        TestFileHandling.removeGeneratedFiles();
    }
}
