package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.ArtificialScenarioCreator;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.amodeus.routing.EuclideanDistanceFunction;

public class ParkingLPVerySimpleTest {

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
        Map<Link, Long> freeSpaces = new HashMap<Link, Long>();
        freeSpaces.put(link2, (long) 10);

        /** solve it */
        ParkingLPSolver parkingLP = new ParkingLPSolver(taxisToGo, freeSpaces, distanceFunction);
        Map<RoboTaxi, Link> solution = parkingLP.returnSolution();

        System.out.println("size: " + solution.size());
        System.out.println(solution.get(roboTaxi).getId());

        /** solution should send roboTaxi to linkup */
        Assert.assertTrue(solution.size() == 1);
        Assert.assertTrue(solution.get(roboTaxi).getId().toString().equals("linkUp"));
    }

}
