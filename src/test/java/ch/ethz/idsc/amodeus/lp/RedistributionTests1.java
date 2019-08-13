/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        for (String origin : solution.keySet()) {
            for (String dest : solution.get(origin).keySet()) {
                System.out.println(origin + " --> " + dest + " flow: " //
                        + solution.get(origin).get(dest));
            }
        }
        Assert.assertTrue(solution.get("o1").get("d1").equals(0));
        Assert.assertTrue(solution.get("o1").get("d2").equals(0));
        Assert.assertTrue(solution.get("o1").get("d3").equals(2));
    }

    @Test
    public void test2() {
        int n1 = 2;
        int n2 = 1;
        Map<String, Map<String, Integer>> solution = localSolver(n1, n2);
        for (String origin : solution.keySet()) {
            for (String dest : solution.get(origin).keySet()) {
                System.out.println(origin + " --> " + dest + " flow: " //
                        + solution.get(origin).get(dest));
            }
        }
        Assert.assertTrue(solution.get("o1").get("d1").equals(2));
        Assert.assertTrue(solution.get("o2").get("d1").equals(2));
    }

    /** helper functions */
    private static double distance(String i1, String i2) {
        return random.nextDouble();
    }

    private static Map<String, Map<String, Integer>> localSolver(int n1, int n2) {
        /** roboTaxi must leave link 1 */
        Map<String, Integer> agentsToGo = new HashMap<>();
        for (int i = 1; i <= n1; ++i) {
            agentsToGo.put("o" + i, 2);
        }

        for (String string : agentsToGo.keySet()) {
            System.out.println("string: " + string);
        }

        /** free spots available on link 2 */
        Map<String, Integer> freeSpaces = new HashMap<>();
        for (int i = 1; i <= n2; ++i) {
            freeSpaces.put("d" + i, 5);
        }

        /** solve it */
        RedistributionProblemSolver<String> redistributionLP = //
                new RedistributionProblemSolver<>(agentsToGo, freeSpaces, //
                        (i1, i2) -> distance(i1, i2), s -> s, false, "");
        Map<String, Map<String, Integer>> solution = redistributionLP.returnSolution();
        return solution;
    }

}
