/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.utils;

import java.util.Objects;

import org.matsim.amodeus.routing.AVRoute;
import org.matsim.amodeus.routing.AVRouteFactory;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

public enum AddCoordinatesToActivities {
    ;
    public static void run(Scenario scenario) {
        for (Person person : scenario.getPopulation().getPersons().values())
            for (Plan plan : person.getPlans())
                for (PlanElement element : plan.getPlanElements())
                    if (element instanceof Activity) {
                        Activity activity = (Activity) element;
                        if (Objects.isNull(activity.getCoord())) {
                            Link link = scenario.getNetwork().getLinks().get(activity.getLinkId());
                            activity.setCoord(link.getCoord());
                        }
                    }
    }

    public static void main(String[] args) {
        String inputPopulationPath = args[0];
        String inputNetworkPath = args[1];
        String outputPopulationPath = args[2];

        Config config = ConfigUtils.createConfig();

        Scenario scenario = ScenarioUtils.createScenario(config);
        scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory(AVRoute.class, new AVRouteFactory());

        new PopulationReader(scenario).readFile(inputPopulationPath);
        new MatsimNetworkReader(scenario.getNetwork()).readFile(inputNetworkPath);

        run(scenario);

        new PopulationWriter(scenario.getPopulation()).write(outputPopulationPath);
    }
}
