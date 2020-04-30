package ch.ethz.matsim.av.routing;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.Controler;

import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.AVScoringParameterSet;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVQSimModule;
import ch.ethz.matsim.av.scenario.TestScenarioGenerator;

public class PreroutingTest {
	@Test
	public void testPreRouting() {
		AVConfigGroup avConfigGroup = new AVConfigGroup();

		AVScoringParameterSet scoringParams = avConfigGroup.getScoringParameters(null);
		scoringParams.setMarginalUtilityOfWaitingTime(-0.84);

		OperatorConfig operatorConfig = new OperatorConfig();
		operatorConfig.setPredictRouteTravelTime(true);
		operatorConfig.getGeneratorConfig().setNumberOfVehicles(100);
		avConfigGroup.addOperator(operatorConfig);

		Config config = ConfigUtils.createConfig(avConfigGroup, new DvrpConfigGroup());
		Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);
		
		config.plansCalcRoute().setRoutingRandomness(0.0);

		PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore().getOrCreateModeParams(AVModule.AV_MODE);
		modeParams.setMonetaryDistanceRate(0.0);
		modeParams.setMarginalUtilityOfTraveling(8.86);
		modeParams.setConstant(0.0);

		StrategySettings strategySettings = new StrategySettings();
		strategySettings.setStrategyName("KeepLastSelected");
		strategySettings.setWeight(1.0);
		config.strategy().addStrategySettings(strategySettings);

		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new DvrpModule());
		controler.addOverridingModule(new AVModule());

		controler.configureQSimComponents(AVQSimModule::configureComponents);

		controler.run();

		for (Person person : scenario.getPopulation().getPersons().values()) {
			Plan plan = person.getSelectedPlan();

			for (PlanElement element : plan.getPlanElements()) {
				if (element instanceof Leg) {
					Leg leg = (Leg) element;
					AVRoute route = (AVRoute) leg.getRoute();

					Assert.assertTrue(
							route.getTravelTime().isDefined() && Double.isFinite(route.getTravelTime().seconds()));
					Assert.assertTrue(Double.isFinite(route.getDistance()));
					Assert.assertTrue(Double.isFinite(route.getWaitingTime()));
					Assert.assertTrue(Double.isFinite(route.getInVehicleTime()));
					Assert.assertTrue(Double.isFinite(route.getPrice()));
				}
			}
		}
	}
}
