package org.matsim.amodeus.routing;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

public class AVRouteTest {
    @BeforeClass
    public static void doYourOneTimeSetup() {
        new File("test_output").mkdir();
    }

    @AfterClass
    public static void doYourOneTimeTeardown() throws IOException {
        FileUtils.deleteDirectory(new File("test_output"));
    }

    @Test
    public void testReadWriteRoute() {
        {
            // Writing file
            Config config = ConfigUtils.createConfig();
            Scenario scenario = ScenarioUtils.createScenario(config);

            Population population = scenario.getPopulation();
            PopulationFactory factory = population.getFactory();

            Person person = factory.createPerson(Id.createPersonId("person"));
            population.addPerson(person);

            Plan plan = factory.createPlan();
            person.addPlan(plan);

            AmodeusRouteFactory routeFactory = new AmodeusRouteFactory();

            Leg leg;
            AmodeusRoute route;

            plan.addActivity(factory.createActivityFromLinkId("whatever", Id.createLinkId("somewhere")));

            leg = factory.createLeg(AmodeusModeConfig.DEFAULT_MODE);
            route = routeFactory.createRoute(Id.createLinkId("S1"), Id.createLinkId("E1"));
            route.setWaitingTime(123.0);
            leg.setRoute(route);
            plan.addLeg(leg);

            plan.addActivity(factory.createActivityFromLinkId("whatever", Id.createLinkId("somewhere")));

            leg = factory.createLeg(AmodeusModeConfig.DEFAULT_MODE);
            route = routeFactory.createRoute(Id.createLinkId("S2"), Id.createLinkId("E2"));
            route.setWaitingTime(987.0);
            leg.setRoute(route);
            plan.addLeg(leg);

            plan.addActivity(factory.createActivityFromLinkId("whatever", Id.createLinkId("somewhere")));

            new PopulationWriter(population).write("test_output/test_population.xml.gz");
        }

        {
            // Reading file
            Config config = ConfigUtils.createConfig();
            Scenario scenario = ScenarioUtils.createScenario(config);

            Population population = scenario.getPopulation();
            population.getFactory().getRouteFactories().setRouteFactory(AmodeusRoute.class, new AmodeusRouteFactory());
            new PopulationReader(scenario).readFile("test_output/test_population.xml.gz");

            Person person = population.getPersons().values().iterator().next();
            Plan plan = person.getPlans().get(0);

            Leg leg1 = (Leg) plan.getPlanElements().get(1);
            Leg leg2 = (Leg) plan.getPlanElements().get(3);

            AmodeusRoute route1 = (AmodeusRoute) leg1.getRoute();
            AmodeusRoute route2 = (AmodeusRoute) leg2.getRoute();

            Assert.assertEquals("S1", route1.getStartLinkId().toString());
            Assert.assertEquals("E1", route1.getEndLinkId().toString());
            Assert.assertEquals(123.0, route1.getWaitingTime().seconds(), 1e-3);

            Assert.assertEquals("S2", route2.getStartLinkId().toString());
            Assert.assertEquals("E2", route2.getEndLinkId().toString());
            Assert.assertEquals(987.0, route2.getWaitingTime().seconds(), 1e-3);
        }
    }
}
