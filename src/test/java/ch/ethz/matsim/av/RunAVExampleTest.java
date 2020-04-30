package ch.ethz.matsim.av;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;
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

import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.AVScoringParameterSet;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.dispatcher.multi_od_heuristic.MultiODHeuristic;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVQSimModule;
import ch.ethz.matsim.av.routing.interaction.LinkAttributeInteractionFinder;
import ch.ethz.matsim.av.scenario.TestScenarioAnalyzer;
import ch.ethz.matsim.av.scenario.TestScenarioGenerator;

public class RunAVExampleTest {
	@Test
	public void testAVExample() {
		AVConfigGroup avConfigGroup = new AVConfigGroup();

		AVScoringParameterSet scoringParams = avConfigGroup.getScoringParameters(null);
		scoringParams.setMarginalUtilityOfWaitingTime(-0.84);

		OperatorConfig operatorConfig = new OperatorConfig();
		operatorConfig.getGeneratorConfig().setNumberOfVehicles(100);
		operatorConfig.getPricingConfig().setPricePerKm(0.48);
		operatorConfig.getPricingConfig().setSpatialBillingInterval(1000.0);
		avConfigGroup.addOperator(operatorConfig);

		Config config = ConfigUtils.createConfig(avConfigGroup, new DvrpConfigGroup());
		Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);

		PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore().getOrCreateModeParams(AVModule.AV_MODE);
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
		controler.addOverridingModule(new AVModule());

		controler.configureQSimComponents(AVQSimModule::configureComponents);

		TestScenarioAnalyzer analyzer = new TestScenarioAnalyzer();
		controler.addOverridingModule(analyzer);

		controler.run();

		Assert.assertEquals(0, analyzer.numberOfDepartures - analyzer.numberOfArrivals);
	}

	@Test
	public void testStuckScoring() {
		AVConfigGroup avConfigGroup = new AVConfigGroup();

		AVScoringParameterSet scoringParams = avConfigGroup.getScoringParameters(null);
		scoringParams.setMarginalUtilityOfWaitingTime(-0.84);

		OperatorConfig operatorConfig = new OperatorConfig();
		operatorConfig.getGeneratorConfig().setNumberOfVehicles(0);
		avConfigGroup.addOperator(operatorConfig);

		Config config = ConfigUtils.createConfig(avConfigGroup, new DvrpConfigGroup());
		Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);
		config.planCalcScore().getOrCreateModeParams(AVModule.AV_MODE);

		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new DvrpModule());
		controler.addOverridingModule(new AVModule());

		controler.configureQSimComponents(AVQSimModule::configureComponents);

		controler.run();

		for (Person person : scenario.getPopulation().getPersons().values()) {
			Assert.assertEquals(-1000.0, person.getSelectedPlan().getScore(), 1e-6);
		}
	}

	@Test
	public void testMultiOD() {
		AVConfigGroup avConfigGroup = new AVConfigGroup();

		AVScoringParameterSet scoringParams = avConfigGroup.getScoringParameters(null);
		scoringParams.setMarginalUtilityOfWaitingTime(-0.84);

		OperatorConfig operatorConfig = new OperatorConfig();
		operatorConfig.getDispatcherConfig().setType(MultiODHeuristic.TYPE);
		operatorConfig.getGeneratorConfig().setNumberOfVehicles(100);
		operatorConfig.getPricingConfig().setPricePerKm(0.48);
		operatorConfig.getPricingConfig().setSpatialBillingInterval(1000.0);
		avConfigGroup.addOperator(operatorConfig);

		Config config = ConfigUtils.createConfig(avConfigGroup, new DvrpConfigGroup());
		Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);

		PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore().getOrCreateModeParams(AVModule.AV_MODE);
		modeParams.setMonetaryDistanceRate(0.0);
		modeParams.setMarginalUtilityOfTraveling(8.86);
		modeParams.setConstant(0.0);

		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new DvrpModule());
		controler.addOverridingModule(new AVModule());

		controler.configureQSimComponents(AVQSimModule::configureComponents);

		TestScenarioAnalyzer analyzer = new TestScenarioAnalyzer();
		controler.addOverridingModule(analyzer);

		controler.run();

		Assert.assertEquals(0, analyzer.numberOfDepartures - analyzer.numberOfArrivals);
	}

	@Test
	public void testAVExampleWithAccessEgress() {
		AVConfigGroup avConfigGroup = new AVConfigGroup();

		AVScoringParameterSet scoringParams = avConfigGroup.getScoringParameters(null);
		scoringParams.setMarginalUtilityOfWaitingTime(-0.84);

		OperatorConfig operatorConfig = new OperatorConfig();
		operatorConfig.getGeneratorConfig().setNumberOfVehicles(100);
		operatorConfig.getPricingConfig().setPricePerKm(0.48);
		operatorConfig.getPricingConfig().setSpatialBillingInterval(1000.0);
		avConfigGroup.addOperator(operatorConfig);

		avConfigGroup.setUseAccessAgress(true);

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

		ActivityParams activityParams = new ActivityParams("av interaction");
		activityParams.setTypicalDuration(1.0);
		config.planCalcScore().addActivityParams(activityParams);

		PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore().getOrCreateModeParams(AVModule.AV_MODE);
		modeParams.setMonetaryDistanceRate(0.0);
		modeParams.setMarginalUtilityOfTraveling(8.86);
		modeParams.setConstant(0.0);

		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new DvrpModule());
		controler.addOverridingModule(new AVModule());

		controler.configureQSimComponents(AVQSimModule::configureComponents);

		TestScenarioAnalyzer analyzer = new TestScenarioAnalyzer();
		controler.addOverridingModule(analyzer);

		controler.run();

		Assert.assertEquals(0, analyzer.numberOfDepartures - analyzer.numberOfArrivals);
		Assert.assertEquals(6, analyzer.numberOfInteractionActivities);
	}

	@Test
	public void testAVExampleWithAccessEgressAttribute() {
		AVConfigGroup avConfigGroup = new AVConfigGroup();

		AVScoringParameterSet scoringParams = avConfigGroup.getScoringParameters(null);
		scoringParams.setMarginalUtilityOfWaitingTime(-0.84);

		OperatorConfig operatorConfig = new OperatorConfig();
		operatorConfig.getGeneratorConfig().setNumberOfVehicles(100);
		operatorConfig.getPricingConfig().setPricePerKm(0.48);
		operatorConfig.getPricingConfig().setSpatialBillingInterval(1000.0);
		operatorConfig.getInteractionFinderConfig().setType(LinkAttributeInteractionFinder.TYPE);
		operatorConfig.getInteractionFinderConfig().getParams().put("allowedLinkAttribute", "avflag");
		avConfigGroup.addOperator(operatorConfig);

		avConfigGroup.setUseAccessAgress(true);

		Config config = ConfigUtils.createConfig(avConfigGroup, new DvrpConfigGroup());
		Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);

		for (Link link : scenario.getNetwork().getLinks().values()) {
			if (link.getFromNode().getCoord().getX() == 5000.0) {
				link.getAttributes().putAttribute("avflag", true);
			}
		}

		ActivityParams activityParams = new ActivityParams("av interaction");
		activityParams.setTypicalDuration(1.0);
		config.planCalcScore().addActivityParams(activityParams);

		PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore().getOrCreateModeParams(AVModule.AV_MODE);
		modeParams.setMonetaryDistanceRate(0.0);
		modeParams.setMarginalUtilityOfTraveling(8.86);
		modeParams.setConstant(0.0);

		config.qsim().setEndTime(40.0 * 3600.0);
		config.qsim().setSimStarttimeInterpretation(StarttimeInterpretation.onlyUseStarttime);

		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new DvrpModule());
		controler.addOverridingModule(new AVModule());

		controler.configureQSimComponents(AVQSimModule::configureComponents);

		TestScenarioAnalyzer analyzer = new TestScenarioAnalyzer();
		controler.addOverridingModule(analyzer);

		controler.run();

		Assert.assertEquals(0, analyzer.numberOfDepartures - analyzer.numberOfArrivals);
		Assert.assertEquals(163, analyzer.numberOfInteractionActivities);
	}
}
