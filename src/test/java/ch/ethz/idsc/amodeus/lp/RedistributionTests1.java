/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.idsc.amodeus.parking.strategies.SmallRedistributionProblemSolver;

public class RedistributionTests1 {

    private static Random random;

    @Before
    public void prepare() {
        random = new Random(10);
    }

    @Test
    public void test1() {
        int n1 = 1;
        int n2 = 3;
        Map<String, Map<String, Integer>> solution = localSolver(n1, n2);
        for (String origin : solution.keySet())
            for (String dest : solution.get(origin).keySet())
                System.out.println(origin + " --> " + dest + " flow: " //
                        + solution.get(origin).get(dest));
        Assert.assertEquals(0, (int) solution.get("o1").get("d1"));
        Assert.assertEquals(0, (int) solution.get("o1").get("d2"));
        Assert.assertEquals(2, (int) solution.get("o1").get("d3"));
    }

    @Test
    public void test2() {
        int n1 = 2;
        int n2 = 1;
        Map<String, Map<String, Integer>> solution = localSolver(n1, n2);
        for (String origin : solution.keySet())
            for (String dest : solution.get(origin).keySet())
                System.out.println(origin + " --> " + dest + " flow: " //
                        + solution.get(origin).get(dest));
        Assert.assertEquals(2, (int) solution.get("o1").get("d1"));
        Assert.assertEquals(2, (int) solution.get("o2").get("d1"));
    }

    /** helper functions */
    private static double distance(String i1, String i2) {
        return random.nextDouble();
    }

    private static Map<String, Map<String, Integer>> localSolver(int n1, int n2) {
        /** roboTaxi must leave link 1 */
        Map<String, Integer> agentsToGo = new HashMap<>();
        for (int i = 1; i <= n1; ++i)
            agentsToGo.put("o" + i, 2);

        for (String string : agentsToGo.keySet())
            System.out.println("string: " + string);

        /** free spots available on link 2 */
        Map<String, Integer> freeSpaces = new HashMap<>();
        for (int i = 1; i <= n2; ++i)
            freeSpaces.put("d" + i, 5);

        /** solve it */
        RedistributionProblemSolver<String> redistributionLP = //
                new RedistributionProblemSolver<>(agentsToGo, freeSpaces, //
                        RedistributionTests1::distance, s -> s, false, "");

        SmallRedistributionProblemSolver<String> smallRedistSolver = //
                new SmallRedistributionProblemSolver<>(agentsToGo, freeSpaces, //
                        RedistributionTests1::distance, s -> s, false, "");
        Assert.assertFalse(smallRedistSolver.success());

        return redistributionLP.returnSolution();
    }

}
