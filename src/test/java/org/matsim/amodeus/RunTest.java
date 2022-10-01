package org.matsim.amodeus;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.amodeus.components.dispatcher.multi_od_heuristic.MultiODHeuristic;
import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.config.modal.AmodeusScoringConfig;
import org.matsim.amodeus.framework.AmodeusModule;
import org.matsim.amodeus.framework.AmodeusQSimModule;
import org.matsim.amodeus.routing.interaction.LinkAttributeInteractionFinder;
import org.matsim.amodeus.scenario.TestScenarioAnalyzer;
import org.matsim.amodeus.scenario.TestScenarioGenerator;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.QSimConfigGroup.StarttimeInterpretation;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.Controler;
import org.matsim.core.utils.geometry.CoordUtils;

public class RunTest {
    @Test
    public void testAVExample() {
        AmodeusConfigGroup avConfigGroup = new AmodeusConfigGroup();

        AmodeusModeConfig operatorConfig = new AmodeusModeConfig(AmodeusModeConfig.DEFAULT_MODE);
        operatorConfig.getGeneratorConfig().setNumberOfVehicles(100);
        operatorConfig.getPricingConfig().setPricePerKm(0.48);
        operatorConfig.getPricingConfig().setSpatialBillingInterval(1000.0);
        avConfigGroup.addMode(operatorConfig);

        AmodeusScoringConfig scoringParams = operatorConfig.getScoringParameters(null);
        scoringParams.setMarginalUtilityOfWaitingTime(-0.84);

        Config config = ConfigUtils.createConfig(avConfigGroup, new DvrpConfigGroup());
        Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);

        config.controler().setWriteEventsInterval(1);

        PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore()
                .getOrCreateModeParams(AmodeusModeConfig.DEFAULT_MODE); // TODO: Refactor
        modeParams.setMonetaryDistanceRate(0.0);
        modeParams.setMarginalUtilityOfTraveling(8.86);
        modeParams.setConstant(0.0);

        config.controler().setLastIteration(2);

        StrategySettings strategySettings = new StrategySettings();
        strategySettings.setStrategyName("KeepLastSelected");
        strategySettings.setWeight(1.0);
        config.strategy().addStrategySettings(strategySettings);

        Controler controler = new Controler(scenario);
        controler.addOverridingModule(new DvrpModule());
        controler.addOverridingModule(new AmodeusModule());
        controler.addOverridingQSimModule(new AmodeusQSimModule());

        controler.configureQSimComponents(AmodeusQSimModule.activateModes(avConfigGroup));

        TestScenarioAnalyzer analyzer = new TestScenarioAnalyzer();
        controler.addOverridingModule(analyzer);

        controler.run();

        Assert.assertEquals(0, analyzer.numberOfDepartures - analyzer.numberOfArrivals);
    }

    @Test
    public void testStuckScoring() {
        AmodeusConfigGroup avConfigGroup = new AmodeusConfigGroup();

        AmodeusModeConfig operatorConfig = new AmodeusModeConfig(AmodeusModeConfig.DEFAULT_MODE);
        operatorConfig.getGeneratorConfig().setNumberOfVehicles(0);
        avConfigGroup.addMode(operatorConfig);

        AmodeusScoringConfig scoringParams = operatorConfig.getScoringParameters(null);
        scoringParams.setMarginalUtilityOfWaitingTime(-0.84);

        Config config = ConfigUtils.createConfig(avConfigGroup, new DvrpConfigGroup());
        Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);
        config.planCalcScore().getOrCreateModeParams(AmodeusModeConfig.DEFAULT_MODE); // Refactor av

        Controler controler = new Controler(scenario);
        controler.addOverridingModule(new DvrpModule());
        controler.addOverridingModule(new AmodeusModule());
        controler.addOverridingQSimModule(new AmodeusQSimModule());

        controler.configureQSimComponents(AmodeusQSimModule.activateModes(avConfigGroup));

        controler.run();

        for (Person person : scenario.getPopulation().getPersons().values()) {
            Assert.assertEquals(-1000.0, person.getSelectedPlan().getScore(), 1e-6);
        }
    }

    @Test
    public void testMultiOD() {
        AmodeusConfigGroup avConfigGroup = new AmodeusConfigGroup();

        AmodeusModeConfig operatorConfig = new AmodeusModeConfig(AmodeusModeConfig.DEFAULT_MODE);
        operatorConfig.getDispatcherConfig().setType(MultiODHeuristic.TYPE);
        operatorConfig.getGeneratorConfig().setNumberOfVehicles(100);
        operatorConfig.getPricingConfig().setPricePerKm(0.48);
        operatorConfig.getPricingConfig().setSpatialBillingInterval(1000.0);
        avConfigGroup.addMode(operatorConfig);

        AmodeusScoringConfig scoringParams = operatorConfig.getScoringParameters(null);
        scoringParams.setMarginalUtilityOfWaitingTime(-0.84);

        Config config = ConfigUtils.createConfig(avConfigGroup, new DvrpConfigGroup());
        Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);

        PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore()
                .getOrCreateModeParams(AmodeusModeConfig.DEFAULT_MODE); // Refactor av
        modeParams.setMonetaryDistanceRate(0.0);
        modeParams.setMarginalUtilityOfTraveling(8.86);
        modeParams.setConstant(0.0);

        Controler controler = new Controler(scenario);
        controler.addOverridingModule(new DvrpModule());
        controler.addOverridingModule(new AmodeusModule());
        controler.addOverridingQSimModule(new AmodeusQSimModule());

        controler.configureQSimComponents(AmodeusQSimModule.activateModes(avConfigGroup));

        TestScenarioAnalyzer analyzer = new TestScenarioAnalyzer();
        controler.addOverridingModule(analyzer);

        controler.run();

        Assert.assertEquals(0, analyzer.numberOfDepartures - analyzer.numberOfArrivals);
    }

    @Test
    public void testAVExampleWithAccessEgress() {
        AmodeusConfigGroup avConfigGroup = new AmodeusConfigGroup();

        AmodeusModeConfig operatorConfig = new AmodeusModeConfig(AmodeusModeConfig.DEFAULT_MODE);
        operatorConfig.getGeneratorConfig().setNumberOfVehicles(100);
        operatorConfig.getPricingConfig().setPricePerKm(0.48);
        operatorConfig.getPricingConfig().setSpatialBillingInterval(1000.0);
        avConfigGroup.addMode(operatorConfig);

        AmodeusScoringConfig scoringParams = operatorConfig.getScoringParameters(null);
        scoringParams.setMarginalUtilityOfWaitingTime(-0.84);

        operatorConfig.setUseAccessAgress(true);

        Config config = ConfigUtils.createConfig(avConfigGroup, new DvrpConfigGroup());
        Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);

        Iterator<? extends Person> iterator = scenario.getPopulation().getPersons().values().iterator();
        for (int i = 0; i < 3; i++) {
            Person person = iterator.next();

            for (PlanElement element : person.getSelectedPlan().getPlanElements()) {
                if (element instanceof Activity) {
                    Activity activity = (Activity) element;
                    activity.setCoord(CoordUtils.plus(activity.getCoord(), new Coord(5.0, 5.0)));
                }
            }
        }

        ActivityParams activityParams = new ActivityParams("amodeus interaction");
        activityParams.setTypicalDuration(1.0);
        config.planCalcScore().addActivityParams(activityParams);

        PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore()
                .getOrCreateModeParams(AmodeusModeConfig.DEFAULT_MODE); // Refactor av
        modeParams.setMonetaryDistanceRate(0.0);
        modeParams.setMarginalUtilityOfTraveling(8.86);
        modeParams.setConstant(0.0);

        Controler controler = new Controler(scenario);
        controler.addOverridingModule(new DvrpModule());
        controler.addOverridingModule(new AmodeusModule());
        controler.addOverridingQSimModule(new AmodeusQSimModule());

        controler.configureQSimComponents(AmodeusQSimModule.activateModes(avConfigGroup));

        TestScenarioAnalyzer analyzer = new TestScenarioAnalyzer();
        controler.addOverridingModule(analyzer);

        controler.run();

        Assert.assertEquals(0, analyzer.numberOfDepartures - analyzer.numberOfArrivals);
        Assert.assertEquals(6, analyzer.numberOfInteractionActivities);
    }

    @Test
    public void testAVExampleWithAccessEgressAttribute() {
        AmodeusConfigGroup avConfigGroup = new AmodeusConfigGroup();

        AmodeusModeConfig operatorConfig = new AmodeusModeConfig(AmodeusModeConfig.DEFAULT_MODE);
        operatorConfig.getGeneratorConfig().setNumberOfVehicles(100);
        operatorConfig.getPricingConfig().setPricePerKm(0.48);
        operatorConfig.getPricingConfig().setSpatialBillingInterval(1000.0);
        operatorConfig.getInteractionFinderConfig().setType(LinkAttributeInteractionFinder.TYPE);
        operatorConfig.getInteractionFinderConfig().getParams().put("allowedLinkAttribute", "avflag");
        avConfigGroup.addMode(operatorConfig);

        AmodeusScoringConfig scoringParams = operatorConfig.getScoringParameters(null);
        scoringParams.setMarginalUtilityOfWaitingTime(-0.84);

        operatorConfig.setUseAccessAgress(true);

        Config config = ConfigUtils.createConfig(avConfigGroup, new DvrpConfigGroup());
        Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);

        for (Link link : scenario.getNetwork().getLinks().values()) {
            if (link.getFromNode().getCoord().getX() == 5000.0) {
                link.getAttributes().putAttribute("avflag", true);
            }
        }

        ActivityParams activityParams = new ActivityParams("amodeus interaction");
        activityParams.setTypicalDuration(1.0);
        config.planCalcScore().addActivityParams(activityParams);

        PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore()
                .getOrCreateModeParams(AmodeusModeConfig.DEFAULT_MODE); // Refactor av
        modeParams.setMonetaryDistanceRate(0.0);
        modeParams.setMarginalUtilityOfTraveling(8.86);
        modeParams.setConstant(0.0);

        config.qsim().setEndTime(40.0 * 3600.0);
        config.qsim().setSimStarttimeInterpretation(StarttimeInterpretation.onlyUseStarttime);

        Controler controler = new Controler(scenario);
        controler.addOverridingModule(new DvrpModule());
        controler.addOverridingModule(new AmodeusModule());
        controler.addOverridingQSimModule(new AmodeusQSimModule());

        controler.configureQSimComponents(AmodeusQSimModule.activateModes(avConfigGroup));

        TestScenarioAnalyzer analyzer = new TestScenarioAnalyzer();
        controler.addOverridingModule(analyzer);

        controler.run();

        Assert.assertEquals(0, analyzer.numberOfDepartures - analyzer.numberOfArrivals);
        Assert.assertEquals(163, analyzer.numberOfInteractionActivities);
    }

    @Test
    public void testBasicSetup() {
        // CONFIG PART

        Config config = ConfigUtils.createConfig(new DvrpConfigGroup(), new AmodeusConfigGroup());

        // Add Amodeus mode
        AmodeusModeConfig modeConfig = new AmodeusModeConfig("av");
        modeConfig.getDispatcherConfig().setType("GlobalBipartiteMatchingDispatcher");
        AmodeusConfigGroup.get(config).addMode(modeConfig);

        config.planCalcScore().getOrCreateModeParams("av");

        // DVRP adjustments
        config.qsim().setSimStarttimeInterpretation(StarttimeInterpretation.onlyUseStarttime);

        // SCENARIO PART

        // Generates a scenario with "av" legs
        Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);

        // CONTROLLER PART
        Controler controller = new Controler(scenario);

        controller.addOverridingModule(new DvrpModule());
        controller.addOverridingModule(new AmodeusModule());

        controller.addOverridingQSimModule(new AmodeusQSimModule());
        controller.configureQSimComponents(
                AmodeusQSimModule.activateModes(AmodeusConfigGroup.get(controller.getConfig())));

        // Some analysis listener for testing
        TestScenarioAnalyzer analyzer = new TestScenarioAnalyzer();
        controller.addOverridingModule(analyzer);

        controller.run();

        Assert.assertEquals(0, analyzer.numberOfDepartures - analyzer.numberOfArrivals);
        Assert.assertEquals(100, analyzer.numberOfDepartures);
    }
}
