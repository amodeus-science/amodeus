/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.prep;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;

import amodeus.amodeus.util.math.Scalar2Number;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.sca.Clip;
import ch.ethz.idsc.tensor.sca.Clips;

// TODO @joel refactor this class
public enum PopulationAVRequests {
    ;

    /** @param population
     * @param network
     * @return the set of all AV requests in the population */
    public static Set<Request> get(Population population, Network network, int endTime) {
        Set<Request> requests = new HashSet<>();
        Clip timeClip = Clips.positive(endTime - 1); // TODO @joel make this more properly using the values from the config file.
        // fill based on population file
        for (Person person : population.getPersons().values()) {
            for (Plan plan : person.getPlans()) {
                if (plan.getPlanElements().size() < 3)
                    continue;
                Iterator<PlanElement> iterator = plan.getPlanElements().iterator();
                PlanElement planElMins;
                PlanElement planElMidl = iterator.next();
                PlanElement planElPlus = iterator.next();
                while (iterator.hasNext()) {
                    planElMins = planElMidl;
                    planElMidl = planElPlus;
                    planElPlus = iterator.next();

                    if (planElMidl instanceof Leg) {
                        Leg leg = (Leg) planElMidl;
                        if (leg.getMode().equals(AmodeusModeConfig.DEFAULT_MODE)) {
                            // get time and vNode index

                            /** if the departure time is not defined for some leg, then
                             * MATSim's LegImpl class returns Double.NEGATIVE_INFINITY, in
                             * that case the end time of the previous activity is used
                             * as the departure time. * */
                            double startTime = leg.getDepartureTime().seconds();
                            if (startTime == Double.NEGATIVE_INFINITY)
                                startTime = Math.max( //
                                        Scalar2Number.of(timeClip.min()).doubleValue(), ((Activity) planElMins).getEndTime().seconds());
                            startTime = Math.min(startTime, Scalar2Number.of(timeClip.max()).doubleValue());
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
