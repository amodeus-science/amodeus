package ch.ethz.idsc.amodeus.lp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.ArtificialScenarioCreator;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.lp.RedistributionProblemSolver;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.amodeus.routing.EuclideanDistanceFunction;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class RedistributionLPVerySimpleTest {

    private static Link link1;
    private static Link link2;
    private static Link link3;
    private static RoboTaxi roboTaxi;

    @Before
    public void prepare() {
        ArtificialScenarioCreator scenario = new ArtificialScenarioCreator();
        link1 = scenario.linkDown;
        link2 = scenario.linkUp;
        link3 = scenario.linkLeft;
        roboTaxi = new RoboTaxi(null, null, link3, null);
    }

    @Test
    public void test() {
        DistanceFunction distanceFunction = EuclideanDistanceFunction.INSTANCE;
        /** roboTaxi must leave link 1 */
        Map<Link, Set<RoboTaxi>> taxisToGo = new HashMap<Link, Set<RoboTaxi>>();
        HashSet<RoboTaxi> taxis = new HashSet<>();
        taxis.add(roboTaxi);
        taxisToGo.put(link1, taxis);

        /** free spots available on link 2 */
        Map<Link, Integer> freeSpaces = new HashMap<>();
        freeSpaces.put(link2, 10);

        /** solve it */
        Map<RoboTaxi, Link> solution = abstractlayer(taxisToGo, freeSpaces, distanceFunction);
        // RedistributionProblemSolver<Link> parkingLP = new RedistributionProblemSolver<Link>(taxisToGo, freeSpaces, //
        // (l1, l2) -> distanceFunction.getDistance(l1, l2), true, "/home/mu/Downloads");
        // Map<RoboTaxi, Link> solution = parkingLP.returnSolution();

        System.out.println("size: " + solution.size());
        System.out.println(solution.get(roboTaxi).getId());

        /** solution should send roboTaxi to linkup */
        Assert.assertTrue(solution.size() == 1);
        Assert.assertTrue(solution.get(roboTaxi).getId().toString().equals("linkUp"));
    }

    private Map<RoboTaxi, Link> abstractlayer(Map<Link, Set<RoboTaxi>> taxisToGo, Map<Link, Integer> freeSpaces, //
            DistanceFunction distanceFunction) {

        /** creating unitsToMove map */
        Map<Link, Integer> unitsToMove = RedistributionProblemHelper.getFlow(taxisToGo);

        /** solve flow problem */
        RedistributionProblemSolver<Link> parkingLP = new RedistributionProblemSolver<>(unitsToMove, freeSpaces, //
                (l1, l2) -> distanceFunction.getDistance(l1, l2), l -> l.getId().toString(), //
                true, "/home/mu/Downloads");
        Map<Link, Map<Link, Integer>> flowSolution = parkingLP.returnSolution();

        /** create roboTaxi movement map */
        return RedistributionProblemHelper.getSolutionCommands(taxisToGo, flowSolution);
    }

}
