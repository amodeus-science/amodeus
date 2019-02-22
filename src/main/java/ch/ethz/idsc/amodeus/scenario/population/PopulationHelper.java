package ch.ethz.idsc.amodeus.scenario.population;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;

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
