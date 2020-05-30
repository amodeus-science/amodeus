package ch.ethz.matsim.av.routing;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.amodeus.config.AmodeusConfigGroup;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.config.modal.AmodeusScoringConfig;
import org.matsim.amodeus.framework.AmodeusModule;
import org.matsim.amodeus.framework.AmodeusQSimModule;
import org.matsim.amodeus.routing.AmodeusRoute;
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

import ch.ethz.matsim.av.scenario.TestScenarioGenerator;

public class PreroutingTest {
    @Test
    public void testPreRouting() {
        AmodeusConfigGroup avConfigGroup = new AmodeusConfigGroup();

        AmodeusModeConfig operatorConfig = new AmodeusModeConfig(AmodeusModeConfig.DEFAULT_MODE);
        operatorConfig.setPredictRouteTravelTime(true);
        operatorConfig.getGeneratorConfig().setNumberOfVehicles(100);
        avConfigGroup.addMode(operatorConfig);

        AmodeusScoringConfig scoringParams = operatorConfig.getScoringParameters(null);
        scoringParams.setMarginalUtilityOfWaitingTime(-0.84);

        operatorConfig.getPricingConfig().setPricePerKm(1.0);

        Config config = ConfigUtils.createConfig(avConfigGroup, new DvrpConfigGroup());
        Scenario scenario = TestScenarioGenerator.generateWithAVLegs(config);

        config.plansCalcRoute().setRoutingRandomness(0.0);

        PlanCalcScoreConfigGroup.ModeParams modeParams = config.planCalcScore().getOrCreateModeParams(AmodeusModeConfig.DEFAULT_MODE);
        modeParams.setMonetaryDistanceRate(0.0);
        modeParams.setMarginalUtilityOfTraveling(8.86);
        modeParams.setConstant(0.0);

        StrategySettings strategySettings = new StrategySettings();
        strategySettings.setStrategyName("KeepLastSelected");
        strategySettings.setWeight(1.0);
        config.strategy().addStrategySettings(strategySettings);

        Controler controler = new Controler(scenario);
        controler.addOverridingModule(new DvrpModule());
        controler.addOverridingModule(new AmodeusModule());
        controler.addOverridingQSimModule(new AmodeusQSimModule());

        controler.configureQSimComponents(AmodeusQSimModule.activateModes(avConfigGroup));

        controler.run();

        for (Person person : scenario.getPopulation().getPersons().values()) {
            Plan plan = person.getSelectedPlan();

            for (PlanElement element : plan.getPlanElements()) {
                if (element instanceof Leg) {
                    Leg leg = (Leg) element;
                    AmodeusRoute route = (AmodeusRoute) leg.getRoute();

                    Assert.assertTrue(route.getTravelTime().isDefined() && Double.isFinite(route.getTravelTime().seconds()));
                    Assert.assertTrue(route.getExpectedDistance().isPresent());
                    Assert.assertTrue(route.getWaitingTime().isDefined());
                    Assert.assertTrue(route.getPrice().isPresent());
                    Assert.assertTrue(route.getPrice().get() > 0.0);
                }
            }
        }
    }
}
