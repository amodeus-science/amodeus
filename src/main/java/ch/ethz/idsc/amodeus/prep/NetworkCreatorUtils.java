/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.router.TripStructureUtils;

public class NetworkCreatorUtils {
    private static final Logger LOGGER = Logger.getLogger(NetworkCreatorUtils.class);

    public static String linkToID(Link link) {
        return link.getId().toString();
    }

    /** @param population
     * @param network
     * @return double of length m,2 with m datapoints and their x,y coordinate where datapoits represent all
     *         Activities of agents in population. */
    public static double[][] fromPopulation(Population population, Network network) {
        // FOR ALL activities find positions, record in list and store in array
        List<double[]> dataList = new ArrayList<>();

        for (Person person : population.getPersons().values())
            for (Plan plan : person.getPlans())
                for (PlanElement planElement : plan.getPlanElements())
                    if (planElement instanceof Activity) {
                        Activity activity = (Activity) planElement;
                        if (!TripStructureUtils.isStageActivityType(activity.getType())) {
                            Link link = network.getLinks().getOrDefault(activity.getLinkId(), null);
                            if (Objects.nonNull(link)) {
                                double x = link.getCoord().getX();
                                double y = link.getCoord().getY();
                                dataList.add(new double[] { x, y });
                            } else {
                                LOGGER.warn(String.format("Link '%s' not found for agent '%s'. Either the link does not exist or has invalid modes?",
                                        activity.getLinkId().toString(), person.getId().toString()));
                            }
                        }
                    }

        // final double data[][] = new double[dataList.size()][2];
        // for (int i = 0; i < dataList.size(); ++i) {
        // data[i][0] = dataList.get(i)[0];
        // data[i][1] = dataList.get(i)[1];
        // }
        // return data;
        return dataList.toArray(new double[dataList.size()][2]);
    }
}
