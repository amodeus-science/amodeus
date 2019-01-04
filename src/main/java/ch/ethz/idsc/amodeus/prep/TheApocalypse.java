/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.network.NetworkUtils;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.sca.Clip;
import ch.ethz.matsim.av.passenger.AVRequest;

/** example use:
 *
 * TheApocalypse.reducesThe(population).toNoMoreThan(1000).people(); */
public final class TheApocalypse {
    /** the seed is deliberately public */
    public static final long DEFAULT_SEED = 7582456789l;
//    public static final long DEFAULT_SEED = 200000l;

    public static TheApocalypse reducesThe(Population population, Network network, int endTime, double minDistance) {
        return new TheApocalypse(population, network, endTime, minDistance);
    }

    // ---
    private final Population population;
    private final Network network;
    private final int endTime;
    private final double minDistance;

    private TheApocalypse(Population population, Network network, int endTime, double minDistance) {
        this.population = population;
        this.network = network;
        this.endTime = endTime;
        this.minDistance = minDistance;
    }

    /** version with seed used so far **/
    public TheApocalypse toNoMoreThan(int maxPrs) {
        return toNoMoreThan(maxPrs, DEFAULT_SEED);
    }

    public TheApocalypse toNoMoreThan(int maxPrs, long seed) {
        List<Id<Person>> list = getFilteredPopulation(network, endTime, minDistance);
//        List<Id<Person>> list = new ArrayList<>(population.getPersons().keySet());
        Collections.shuffle(list, new Random(seed));
        final int sizeAnte = list.size();
        list.stream() //
                .limit(Math.max(0, sizeAnte - maxPrs)) //
                .forEach(population::removePerson);
        final int sizePost = population.getPersons().size();
        GlobalAssert.that(sizePost <= maxPrs);
        return this;
    }

    public final void people() {
        System.out.println("Population size: " + population.getPersons().values().size());
    }
    
    private List<Id<Person>> getFilteredPopulation(Network network, int endTime, double minDistance) {
        List<Id<Person>> list = new ArrayList<>();
        List<Id<Person>> removedList = new ArrayList<>();
        Clip timeClip = Clip.function(0, endTime - 1);
        boolean isFar = false;
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
                            timeClip.requireInside(RealScalar.of(startTime));

                            Link startLink = network.getLinks().get(((Activity) planElMins).getLinkId());
                            Link endLink = network.getLinks().get(((Activity) planElPlus).getLinkId());
                            double distance = distanceLinks(startLink, endLink);
                            if(distance >= minDistance) {
                                isFar = true;
                            } else {
                                isFar = false;
                                break;
                            }
                        }
                    }
                }
                
                if(isFar==false) {
                    break;
                }
            }
            if(isFar==true) {
                list.add(person.getId());
            } else {
                removedList.add(person.getId());
            }
        }
        removedList.forEach(population::removePerson);
        return list;
    }
    
    static double distanceLinks(Link link1, Link link2) {
        return NetworkUtils.getEuclideanDistance( //
                link1.getCoord(), //
                link2.getCoord());
    }
}
