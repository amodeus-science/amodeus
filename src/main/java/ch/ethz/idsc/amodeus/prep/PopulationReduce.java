/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.util.Iterator;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;

public enum PopulationReduce {
    ;

    public static void outsideNetwork(Population population, Network network) {
        System.out.println("All people in population  which have activities outside network are removed.");

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
}
