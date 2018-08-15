/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;

public enum PopulationTools {
    ;
    private static final int DAYDURATION = 24 * 60 * 60;
    private static final Logger log = Logger.getLogger(PopulationTools.class);

    public static void removeOutsideNetwork(Population population, Network network) {

        log.info("All people in population  which have activities outside network are removed.");

        Iterator<? extends Person> itPerson = population.getPersons().values().iterator();

        int counter = 0;
        int nextMsg = 1;

        while (itPerson.hasNext()) {
            counter++;
            if (counter % nextMsg == 0) {
                nextMsg *= 2;
                System.out.println("we are at person # " + counter + ". ");
            }
            Person person = itPerson.next();
            boolean removePerson = false;
            for (Plan plan : person.getPlans()) {
                for (PlanElement planElement : plan.getPlanElements()) {
                    if (planElement instanceof Activity) {
                        Activity act = (Activity) planElement;

                        Id<Link> actLink = act.getLinkId();
                        if (!network.getLinks().containsKey(actLink)) {
                            removePerson = true;
                            break;
                        }
                    }
                }
            }
            if (removePerson)
                itPerson.remove();
        }
    }

    /** @param population
     * @param network
     * @return the set of all AV requests in the population */
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
                            double startTime = leg.getDepartureTime();
                            if (startTime == Double.NEGATIVE_INFINITY) {
                                Activity actBefore = (Activity) planElMins;
                                startTime = actBefore.getEndTime();
                            }
                            GlobalAssert.that(startTime >= 0);

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

    /** @param requests
     * @param virtualNetwork
     * @param timeInterval
     * @return {@link Tensor} with indices k,i,j where the elements are the number of requests from virtual station i to j at time interval k. E.g. (5,1,2)=10 means
     *         that 10 requests appear in virtual station i with destination in virtual station j at time interval 5. */
    public static Tensor getLambdaInVirtualNodesAndTimeIntervals(Set<Request> requests, VirtualNetwork<Link> virtualNetwork, int timeInterval) {
        GlobalAssert.that(DAYDURATION % timeInterval == 0);

        Tensor lambda = Array.zeros(DAYDURATION / timeInterval, virtualNetwork.getvNodesCount(), virtualNetwork.getvNodesCount());

        for (Request request : requests) {
            int timeIndex = (int) Math.floor(request.startTime() / timeInterval);
            int vNodeIndexFrom = virtualNetwork.getVirtualNode(request.startLink()).getIndex();
            int vNodeIndexTo = virtualNetwork.getVirtualNode(request.endLink()).getIndex();

            // add customer to matrix
            lambda.set(s -> s.add(RealScalar.ONE), timeIndex, vNodeIndexFrom, vNodeIndexTo);
        }

        return lambda;
    }
}
