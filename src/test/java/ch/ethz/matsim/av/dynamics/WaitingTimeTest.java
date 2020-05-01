package ch.ethz.matsim.av.dynamics;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;

import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.AVScoringParameterSet;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVQSimModule;
import ch.ethz.matsim.av.routing.AVRoute;
import ch.ethz.matsim.av.scenario.TestScenarioGenerator;

public class WaitingTimeTest {
	static AVConfigGroup createConfig() {
		AVConfigGroup avConfigGroup = new AVConfigGroup();

		AVScoringParameterSet scoringParams = avConfigGroup.getScoringParameters(null);
		scoringParams.setMarginalUtilityOfWaitingTime(-0.84);

		OperatorConfig operatorConfig = new OperatorConfig();
		operatorConfig.getGeneratorConfig().setNumberOfVehicles(100);
		operatorConfig.getPricingConfig().setPricePerKm(0.48);
		operatorConfig.getPricingConfig().setSpatialBillingInterval(1000.0);
		avConfigGroup.addOperator(operatorConfig);

		return avConfigGroup;
	}

	static Controler createController(AVConfigGroup avConfigGroup) {
		Config config = ConfigUtils.createConfig(avConfigGroup, new DvrpConfigGroup());
		Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);

		PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore().getOrCreateModeParams(AVModule.AV_MODE);
		modeParams.setMonetaryDistanceRate(0.0);
		modeParams.setMarginalUtilityOfTraveling(8.86);
		modeParams.setConstant(0.0);

		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new DvrpModule());
		controler.addOverridingModule(new AVModule());
		controler.addOverridingQSimModule(new AVQSimModule());

		controler.configureQSimComponents(AVQSimModule::configureComponents);

		return controler;
	}

	@Test
	public void testConstantWaitingTime() {
		AVConfigGroup config = createConfig();
		OperatorConfig operatorConfig = config.getOperatorConfigs().get(OperatorConfig.DEFAULT_OPERATOR_ID);

		operatorConfig.getWaitingTimeConfig().setDefaultWaitingTime(123.0);

		Controler controller = createController(config);
		controller.run();

		Population population = controller.getScenario().getPopulation();

		int numberOfRoutes = 0;

		for (Person person : population.getPersons().values()) {
			Plan plan = person.getSelectedPlan();

			for (PlanElement element : plan.getPlanElements()) {
				if (element instanceof Leg) {
					Leg leg = (Leg) element;
					AVRoute route = (AVRoute) leg.getRoute();

					Assert.assertEquals(route.getWaitingTime(), 123.0, 1e-2);
					numberOfRoutes++;
				}
			}
		}

		Assert.assertEquals(100, numberOfRoutes);
	}

	@Test
	public void testAttributeWaitingTime() {
		AVConfigGroup config = createConfig();
		OperatorConfig operatorConfig = config.getOperatorConfigs().get(OperatorConfig.DEFAULT_OPERATOR_ID);

		operatorConfig.getWaitingTimeConfig().setDefaultWaitingTime(123.0);
		operatorConfig.getWaitingTimeConfig().setConstantWaitingTimeLinkAttribute("avWaitingTime");

		Controler controller = createController(config);

		Link link = controller.getScenario().getNetwork().getLinks().get(Id.createLinkId("8:9_9:9"));
		link.getAttributes().putAttribute("avWaitingTime", 456.0);

		controller.run();

		Population population = controller.getScenario().getPopulation();

		int numberOfRoutes = 0;
		int numberOfSpecialRoutes = 0;

		for (Person person : population.getPersons().values()) {
			Plan plan = person.getSelectedPlan();

			for (PlanElement element : plan.getPlanElements()) {
				if (element instanceof Leg) {
					Leg leg = (Leg) element;
					AVRoute route = (AVRoute) leg.getRoute();

					if (Id.createLinkId("8:9_9:9").equals(route.getStartLinkId())) {
						Assert.assertEquals(route.getWaitingTime(), 456.0, 1e-2);
						numberOfSpecialRoutes++;
					} else {
						Assert.assertEquals(route.getWaitingTime(), 123.0, 1e-2);
					}

					numberOfRoutes++;
				}
			}
		}

		Assert.assertEquals(100, numberOfRoutes);
		Assert.assertEquals(2, numberOfSpecialRoutes);
	}

	@Test
	public void testDynamicWaitingTime() {
		AVConfigGroup config = createConfig();
		OperatorConfig operatorConfig = config.getOperatorConfigs().get(OperatorConfig.DEFAULT_OPERATOR_ID);

		operatorConfig.getWaitingTimeConfig().setDefaultWaitingTime(123.0);
		operatorConfig.getWaitingTimeConfig().setConstantWaitingTimeLinkAttribute("avWaitingTime");
		operatorConfig.getWaitingTimeConfig().setEstimationLinkAttribute("avGroup");
		operatorConfig.getWaitingTimeConfig().setEstimationAlpha(0.7);

		Controler controller = createController(config);

		Link link = controller.getScenario().getNetwork().getLinks().get(Id.createLinkId("8:9_9:9"));
		link.getAttributes().putAttribute("avWaitingTime", 456.0);

		int index = 0;
		for (Link _link : controller.getScenario().getNetwork().getLinks().values()) {
			_link.getAttributes().putAttribute("avGroup", index++);
		}

		controller.getConfig().controler().setLastIteration(2);

		StrategySettings strategy = new StrategySettings();
		strategy.setStrategyName("ReRoute");
		strategy.setWeight(1.0);
		controller.getConfig().strategy().addStrategySettings(strategy);

		List<Double> waitingTimes = new LinkedList<>();

		controller.addControlerListener(new IterationEndsListener() {
			@Override
			public void notifyIterationEnds(IterationEndsEvent event) {
				Population population = event.getServices().getScenario().getPopulation();
				Person person = population.getPersons().get(Id.createPersonId(17));
				Plan plan = person.getSelectedPlan();

				for (PlanElement element : plan.getPlanElements()) {
					if (element instanceof Leg) {
						Leg leg = (Leg) element;
						AVRoute route = (AVRoute) leg.getRoute();

						if (Id.createLinkId("8:9_9:9").equals(route.getStartLinkId())) {
							waitingTimes.add(route.getWaitingTime());
						}
					}
				}
			}
		});

		controller.run();

		Assert.assertEquals(456.0, waitingTimes.get(0), 1e-3);
		Assert.assertEquals(144.5, waitingTimes.get(1), 1e-3);
		Assert.assertEquals(51.05, waitingTimes.get(2), 1e-3);
	}
	
	@Test
	public void testDynamicWaitingTimeWithoutConstantAttribute() {
		AVConfigGroup config = createConfig();
		OperatorConfig operatorConfig = config.getOperatorConfigs().get(OperatorConfig.DEFAULT_OPERATOR_ID);

		operatorConfig.getWaitingTimeConfig().setDefaultWaitingTime(123.0);
		operatorConfig.getWaitingTimeConfig().setEstimationLinkAttribute("avGroup");
		operatorConfig.getWaitingTimeConfig().setEstimationAlpha(0.7);

		Controler controller = createController(config);

		Link link = controller.getScenario().getNetwork().getLinks().get(Id.createLinkId("8:9_9:9"));
		link.getAttributes().putAttribute("avWaitingTime", 456.0);

		int index = 0;
		for (Link _link : controller.getScenario().getNetwork().getLinks().values()) {
			_link.getAttributes().putAttribute("avGroup", index++);
		}

		controller.getConfig().controler().setLastIteration(2);

		StrategySettings strategy = new StrategySettings();
		strategy.setStrategyName("ReRoute");
		strategy.setWeight(1.0);
		controller.getConfig().strategy().addStrategySettings(strategy);

		List<Double> waitingTimes = new LinkedList<>();

		controller.addControlerListener(new IterationEndsListener() {
			@Override
			public void notifyIterationEnds(IterationEndsEvent event) {
				Population population = event.getServices().getScenario().getPopulation();
				Person person = population.getPersons().get(Id.createPersonId(17));
				Plan plan = person.getSelectedPlan();

				for (PlanElement element : plan.getPlanElements()) {
					if (element instanceof Leg) {
						Leg leg = (Leg) element;
						AVRoute route = (AVRoute) leg.getRoute();

						if (Id.createLinkId("8:9_9:9").equals(route.getStartLinkId())) {
							waitingTimes.add(route.getWaitingTime());
						}
					}
				}
			}
		});

		controller.run();

		Assert.assertEquals(123.0, waitingTimes.get(0), 1e-3);
		Assert.assertEquals(44.6, waitingTimes.get(1), 1e-3);
		Assert.assertEquals(21.08, waitingTimes.get(2), 1e-3);
	}

	@AfterClass
	public static void doYourOneTimeTeardown() throws IOException {
		FileUtils.deleteDirectory(new File(TestScenarioGenerator.outputDir));
	}
}
