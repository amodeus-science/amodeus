/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.idsc.amodeus.parking.strategies.SmallRedistributionProblemSolver;

public class SmallRedistributionTests {

    private static Random random;

    @Before
    public void prepare() {
        random = new Random(10);
    }

    @Test
    public void test1() {
        int n1 = 1;
        int n2 = 2;
        Map<String, Map<String, Integer>> solution = localSolver(n1, n2);
        for (String origin : solution.keySet()) {
            for (String dest : solution.get(origin).keySet()) {
                System.out.println(origin + " --> " + dest + ", flow:  " //
                        + solution.get(origin).get(dest));
            }
        }

 
        Assert.assertTrue(solution.get("o1").get("d1").equals(1));

    }

    /** helper functions */

    private static double distance(String i1, String i2) {
        if (i2.equals("d2"))
            return 10;
        return random.nextDouble();
    }

    private static Map<String, Map<String, Integer>> localSolver(int n1, int n2) {
        /** roboTaxi must leave link 1 */
        Map<String, Integer> agentsToGo = new HashMap<>();
        for (int i = 1; i <= n1; ++i) {
            agentsToGo.put("o" + i, 1);
        }

        System.out.println("Agents to go: ");
        for (Entry<String, Integer> entry : agentsToGo.entrySet()) {
            System.out.println(entry.getKey() + ", " + entry.getValue());
        }
        System.out.println("===");

        /** free spots available on link 2 */
        Map<String, Integer> freeSpaces = new HashMap<>();
        for (int i = 1; i <= n2; ++i) {
            freeSpaces.put("d" + i, i + 1);
        }

        System.out.println("Free spaces: ");
        for (Entry<String, Integer> entry : freeSpaces.entrySet()) {
            System.out.println(entry.getKey() + ", " + entry.getValue());
        }
        System.out.println("===");

        /** solve it */
        SmallRedistributionProblemSolver<String> redistributionLP = //
                new SmallRedistributionProblemSolver<>(agentsToGo, freeSpaces, //
                        (i1, i2) -> distance(i1, i2), s -> s, false, "");
        Map<String, Map<String, Integer>> solution = redistributionLP.returnSolution();
        return solution;
    }

}
