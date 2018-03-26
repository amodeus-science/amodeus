/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim;

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
import org.matsim.api.core.v01.population.Population;
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
import ch.ethz.idsc.amodeus.prep.MatsimKMEANSVirtualNetworkCreator;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
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
                { "AdaptiveRealTimeRebalancingPolicy" }, { "FeedforwardFluidicRebalancingPolicy" } });
    }

    final private String dispatcher;

    public StandardMATSimScenarioTest(String dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Test
    public void testStandardMATSimScenario() {
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

        Controler controler = new Controler(scenario);
        controler.addOverridingModule(new DvrpTravelTimeModule());
        controler.addOverridingModule(new AVModule());
        controler.addOverridingModule(new AmodeusModule());
        controler.addOverridingModule(new AmodeusDispatcherModule());

        // Set up a virtual network for the LPFBDispatcher

        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                // ---
            }

            @Provides
            @Singleton
            public VirtualNetwork<Link> provideVirtualNetwork() {
                // Since we have no virtual netowrk saved in the working directory for our test
                // sceanario, we need to provide a custom one for the LPFB dispatcher

                return MatsimKMEANSVirtualNetworkCreator.createVirtualNetwork(scenario.getPopulation(), scenario.getNetwork(), 2, true);
            }
            
            @Provides
            @Singleton
            public TravelData provideTravelData(VirtualNetwork<Link> virtualNetwork, @Named(AVModule.AV_MODE) Network network, Population population ) {
                // Same as for the virtual network: For the LPFF dispatcher we need travel 
                // data, which we generate on the fly here.
                
                TravelData travelData = new TravelData(virtualNetwork, network, population, 300);
                return travelData;
            }
        });

        // Config

        AVConfig avConfig = new AVConfig();
        AVOperatorConfig operatorConfig = avConfig.createOperatorConfig("test");
        AVGeneratorConfig generatorConfig = operatorConfig.createGeneratorConfig("PopulationDensity");
        generatorConfig.setNumberOfVehicles(100);

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

        // Set up test analyzer and run

        TestScenarioAnalyzer analyzer = new TestScenarioAnalyzer();
        controler.addOverridingModule(analyzer);

        controler.run();
        Assert.assertEquals(0, analyzer.numberOfDepartures - analyzer.numberOfArrivals);
    }
}
