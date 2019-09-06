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

public enum PopulationRemove {
    ;

    public static void outsideNetwork(Population population, Network network) {
        System.out.println("All people in population with activities outside the network are removed.");
        int sizeBefore = population.getPersons().size();
        Iterator<? extends Person> itPerson = population.getPersons().values().iterator();
        while (itPerson.hasNext()) {
            Person person = itPerson.next();
            boolean removePerson = false;
            for (Plan plan : person.getPlans()) {
                for (PlanElement planElement : plan.getPlanElements()) {
                    if (planElement instanceof Activity) {
                        Activity act = (Activity) planElement;
                        Id<Link> linkId = act.getLinkId();
                        if (!network.getLinks().containsKey(linkId)) {
                            removePerson = true;
                            break;
                        }
                    }
                }
            }
            if (removePerson)
                itPerson.remove();
        }
        System.out.println("Population size before:  " + sizeBefore);
        System.out.println("Population size after:   " + population.getPersons().size());
    }
}
