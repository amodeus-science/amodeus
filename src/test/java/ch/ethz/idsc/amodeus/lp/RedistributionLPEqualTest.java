package ch.ethz.idsc.amodeus.lp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class RedistributionLPEqualTest {

    private static final Random random = new Random(10);

    // @Test
    // public void test1() throws InterruptedException {
    // int n1 = 1;
    // int n2 = 2;
    // Map<String, String> solution = localSolver(n1, n2);
    // for (Entry<String, String> entry : solution.entrySet()) {
    // System.out.println(entry.getKey() + ", " + entry.getValue());
    // }
    // Assert.assertTrue(solution.get("agent_1").equals("destinLot_2"));
    // }

    @Test
    public void test2() throws InterruptedException {
        int n1 = 2;
        int n2 = 2;
        Map<String, String> solution = localSolver(n1, n2);
        for (Entry<String, String> entry : solution.entrySet()) {
            System.out.println(entry.getKey() + ", " + entry.getValue());
        }
        Assert.assertTrue(solution.get("agent_0").equals(2));
    }

    /** helper functions */

    private static double distance(String i1, String i2) {
        return random.nextDouble();
    }

    private static Map<String, String> localSolver(int n1, int n2) {
        /** roboTaxi must leave link 1 */
        Map<String, Set<String>> agentsToGo = new HashMap<>();
        for (int i = 1; i <= n1; ++i) {
            HashSet<String> agents = new HashSet<>();
            agents.add("agent_" + i);
            agentsToGo.put("o" + i, agents);
        }

        for (String string : agentsToGo.keySet()) {
            System.out.println("string: " + string);
        }

        /** free spots available on link 2 */
        Map<String, Long> freeSpaces = new HashMap<>();
        // for (int i = n1; i < n1 + n2; ++i) {
        for (int i = 1; i <= n2; ++i) {
            freeSpaces.put("d" + i, (long) i + 1);
        }
        /** solve it */
        RedistributionProblemSolver<String, String> redistributionLP = //
                new RedistributionProblemSolver<>(agentsToGo, freeSpaces, //
                        (i1, i2) -> distance(i1, i2), true, "/home/mu/Downloads");
        Map<String, String> solution = redistributionLP.returnSolution();
        return solution;
    }

}
