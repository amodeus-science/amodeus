/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.prep;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;

public enum PopulationRemove {
    ;

    public static void outsideNetwork(Population population, Network network) {
        System.out.println("All people in population with activities outside the network are removed.");
        int sizeBefore = population.getPersons().size();
        // Iterator<? extends Person> itPerson = population.getPersons().values().iterator();
        // while (itPerson.hasNext()) {
        // Person person = itPerson.next();
        // boolean removePerson = false;
        // for (Plan plan : person.getPlans()) {
        // for (PlanElement planElement : plan.getPlanElements()) {
        // if (planElement instanceof Activity) {
        // Activity act = (Activity) planElement;
        // Id<Link> linkId = act.getLinkId();
        // if (!network.getLinks().containsKey(linkId)) {
        // removePerson = true;
        // break;
        // }
        // }
        // }
        // }
        // if (removePerson)
        // itPerson.remove();
        // }
        population.getPersons().entrySet().removeIf(e -> {
            Person person = e.getValue();
            return !person.getPlans().stream().flatMap(plan -> // for all plans of a person
            // for all link ids covered by the plan
            plan.getPlanElements().stream().filter(pe -> pe instanceof Activity).map(pe -> (Activity) pe).map(Activity::getLinkId)).allMatch(network.getLinks()::containsKey); // are
                                                                                                                                                                               // all
                                                                                                                                                                               // link
                                                                                                                                                                               // ids
                                                                                                                                                                               // in
                                                                                                                                                                               // the
                                                                                                                                                                               // network
        });
        System.out.println("Population size before:  " + sizeBefore);
        System.out.println("Population size after:   " + population.getPersons().size());
    }
}
