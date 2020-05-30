package ch.ethz.idsc.amodeus.util.matsim;

import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.collections.QuadTrees;

public class SnapToClosestNetworkLink {
    private SnapToClosestNetworkLink() {
    }

    static public void run(Population population, Network network, String mode) {
        QuadTree<? extends Link> index = QuadTrees
                .createQuadTree(network.getLinks().values().stream().filter(link -> link.getAllowedModes().contains(mode)).collect(Collectors.toList()));

        for (Person person : population.getPersons().values()) {
            for (Plan plan : person.getPlans()) {
                for (PlanElement element : plan.getPlanElements()) {
                    if (element instanceof Activity) {
                        Activity activity = (Activity) element;

                        if (activity.getLinkId() != null) {
                            Link link = network.getLinks().get(activity.getLinkId());

                            if (!link.getAllowedModes().contains(mode)) {
                                Link closestLink = index.getClosest(link.getCoord().getX(), link.getCoord().getY());
                                activity.setLinkId(closestLink.getId());
                            }
                        }
                    }
                }
            }
        }
    }
}
