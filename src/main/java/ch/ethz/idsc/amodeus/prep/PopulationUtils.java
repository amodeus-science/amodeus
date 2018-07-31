package ch.ethz.idsc.amodeus.prep;

import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class PopulationUtils {

    public static Set<Request> getAVRequests(Population population, Network network) {
        Set<Request> requests = new HashSet<>();
        // fill based on population file
        for (Person person : population.getPersons().values()) {
            for (Plan plan : person.getPlans()) {

                for (int i = 1; i < plan.getPlanElements().size() - 1; ++i) {
                    PlanElement planElMins = plan.getPlanElements().get(i - 1);
                    PlanElement planElMidl = plan.getPlanElements().get(i);
                    PlanElement planElPlus = plan.getPlanElements().get(i + 1);

                    if (planElMidl instanceof Leg) {
                        Leg leg = (Leg) planElMidl;
                        if (leg.getMode().equals("av")) {
                            // get time and vNode index

                            /** if the departure time is not defined for some leg, then
                             * MATSim's LegImpl class returns Double.NEGATIVE_INFINITY, in
                             * that case the end time of the previous activity is used
                             * as the departure time. * */
                            double depTime = leg.getDepartureTime();
                            if (depTime == Double.NEGATIVE_INFINITY) {
                                Activity actBefore = (Activity) planElMins;
                                depTime = actBefore.getEndTime();
                            }
                            GlobalAssert.that(depTime >= 0);

                            Link depLink = network.getLinks().get(((Activity) planElMins).getLinkId());
                            requests.add(new Request(depTime, depLink));
                        }
                    }
                }
            }
        }
        return requests;
    }
}
