/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.capacities;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;

public class ParkingCapacityUniformRandomPopulationZone extends ParkingCapacityAbstractUniform {

    /** assigns totSpaces randomly chosen links from the network a parking space, there may
     * be multiple parking spaces per link */
    public ParkingCapacityUniformRandomPopulationZone(Network network, Population population, //
            long totSpaces, Random random) {
        super(network, population, totSpaces, random);
    }

    @Override
    protected Collection<? extends Link> getLinks(Network network, Population population) {
        HashSet<Link> populatedLinks = new HashSet<>();
        for (Person person : population.getPersons().values())
            for (Plan plan : person.getPlans())
                for (PlanElement planElement : plan.getPlanElements())
                    if (planElement instanceof Activity) {
                        Activity activity = (Activity) planElement;
                        populatedLinks.add(network.getLinks().get(activity.getLinkId()));
                    }
        return populatedLinks;
    }
}
