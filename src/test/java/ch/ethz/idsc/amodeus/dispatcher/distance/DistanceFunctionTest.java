/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.distance;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.gbl.MatsimRandom;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.matsim.mod.AmodeusDispatcherModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusModule;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.prep.MatsimKMEANSVirtualNetworkCreator;
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
public class DistanceFunctionTest {
    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { "euclidean" }, { "dijkstra" }, { "astar_euclidean" }, { "astar_landmarks" } });
    }

    final private String distanceFunctionName;

    public DistanceFunctionTest(String distanceFunctionName) {
        this.distanceFunctionName = distanceFunctionName;
    }

    @Test
    public void testDistanceFunction() {
        /* This test runs a small test scenario with the different dispatchers and makes
         * sure that all 100 generated agents arrive */

        MatsimRandom.reset();

        // Set up
        Config config = ConfigUtils.createConfig(new AVConfigGroup(), new DvrpConfigGroup());
        Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);

        PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore().getOrCreateModeParams(AVModule.AV_MODE);
        modeParams.setMonetaryDistanceRate(0.0);
        modeParams.setMarginalUtilityOfTraveling(8.86);
        modeParams.setConstant(0.0);

        ScenarioOptions scenarioOptions = ScenarioOptions.create();
        scenarioOptions.setProperty("numberOfAStarLandmarks", "4");

        Controler controler = new Controler(scenario);
        controler.addOverridingModule(new DvrpTravelTimeModule());
        controler.addOverridingModule(new AVModule());
        controler.addOverridingModule(new AmodeusModule(scenarioOptions));
        controler.addOverridingModule(new AmodeusDispatcherModule());

        // Set up a virtual network for the LPFBDispatcher

        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                // ---
            }

            @Provides
            @Singleton
            public VirtualNetwork<Link> provideVirtualNetwork(@Named(AVModule.AV_MODE) Network network) {
                return MatsimKMEANSVirtualNetworkCreator.createVirtualNetwork(scenario.getPopulation(), network, 2, true);
            }
        });

        // Config

        AVConfig avConfig = new AVConfig();
        AVOperatorConfig operatorConfig = avConfig.createOperatorConfig("test");
        AVGeneratorConfig generatorConfig = operatorConfig.createGeneratorConfig("PopulationDensity");
        generatorConfig.setNumberOfVehicles(100);

        // Choose a dispatcher
        AVDispatcherConfig dispatcherConfig = operatorConfig.createDispatcherConfig("GlobalBipartiteMatchingDispatcher");

        // Make sure that we do not need the SimulationObjectCompiler
        dispatcherConfig.addParam("publishPeriod", "-1");

        // Set distance function
        dispatcherConfig.addParam("distanceFunction", distanceFunctionName);

        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                bind(AVConfig.class).toInstance(avConfig);
            }
        });

        // Set up test analyzer and run
        TestScenarioAnalyzer analyzer = new TestScenarioAnalyzer();
        controler.addOverridingModule(analyzer);

        controler.run();
        Assert.assertEquals(0, analyzer.numberOfDepartures - analyzer.numberOfArrivals);
    }
}
