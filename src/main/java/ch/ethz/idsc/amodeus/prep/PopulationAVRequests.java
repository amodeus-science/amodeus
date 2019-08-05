/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
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

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.sca.Clip;
import ch.ethz.idsc.tensor.sca.Clips;

// TODO refactor this class
public enum PopulationAVRequests {
    ;

    /** @param population
     * @param network
     * @return the set of all AV requests in the population */
    public static Set<Request> get(Population population, Network network, int endTime) {
        Set<Request> requests = new HashSet<>();
        Clip timeClip = Clips.positive(endTime - 1);
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
                            double startTime = leg.getDepartureTime();
                            if (startTime == Double.NEGATIVE_INFINITY) {
                                Activity actBefore = (Activity) planElMins;
                                // TODO make this more properly using the values from the config file.
                                startTime = Math.max(0.0, actBefore.getEndTime());
                            }
                            if (startTime > 107999.0) {
                                // TODO make this more properly using the values from the config file.
                                startTime = 107999.0;
                            }

                            timeClip.requireInside(RealScalar.of(startTime));

                            Link startLink = network.getLinks().get(((Activity) planElMins).getLinkId());
                            Link endLink = network.getLinks().get(((Activity) planElPlus).getLinkId());
                            requests.add(new Request(startTime, startLink, endLink));
                        }
                    }
                }
            }
        }
        return requests;
    }
}
