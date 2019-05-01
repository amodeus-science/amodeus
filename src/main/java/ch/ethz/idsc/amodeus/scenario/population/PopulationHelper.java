/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.scenario.population;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;

/* package */ enum PopulationHelper {
    ;

    /** Checks if all the population's activities are inside the network
     * 
     * @param population
     * @param network
     * @return boolean */
    public static boolean checkAllActivitiesInNetwork(Population population, Network network) {
        for (Person person : population.getPersons().values()) {
            for (Plan plan : person.getPlans()) {
                for (PlanElement pE : plan.getPlanElements()) {
                    if (pE instanceof Activity) {
                        Activity activity = (Activity) pE;
                        if (!network.getLinks().containsKey(activity.getLinkId())) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
