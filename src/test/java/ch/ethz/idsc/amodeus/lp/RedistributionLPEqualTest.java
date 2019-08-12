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

//    @Test
//    public void test1() throws InterruptedException {
//        int n1 = 1;
//        int n2 = 2;
//        Map<String, Integer> solution = localSolver(n1, n2);
//        for (Entry<String, Integer> entry : solution.entrySet()) {
//            System.out.println(entry.getKey() + ",  " + entry.getValue());
//        }
//        Assert.assertTrue(solution.get("agent_0").equals(2));
//
//    }
    
    @Test
    public void test1() throws InterruptedException {
        int n1 = 2;
        int n2 = 2;
        Map<String, Integer> solution = localSolver(n1, n2);
        for (Entry<String, Integer> entry : solution.entrySet()) {
            System.out.println(entry.getKey() + ",  " + entry.getValue());
        }
        Assert.assertTrue(solution.get("agent_0").equals(2));

    }

    /** helper functions */

    private static double distance(Integer i1, Integer i2) {
        return random.nextDouble();
    }

    private static Map<String, Integer> localSolver(int n1, int n2) {
        /** roboTaxi must leave link 1 */
        Map<Integer, Set<String>> agentsToGo = new HashMap<>();
        for (int i = 0; i < n1; ++i) {
            HashSet<String> agents = new HashSet<>();
            agents.add("agent_" + i);
            agentsToGo.put(i, agents);
        }

        /** free spots available on link 2 */
        Map<Integer, Long> freeSpaces = new HashMap<>();
        for (int i = n1; i < n1 + n2; ++i) {
            freeSpaces.put(i, (long) i);
        }
        /** solve it */
        RedistributionProblemSolver<Integer, String> redistributionLP = //
                new RedistributionProblemSolver<>(agentsToGo, freeSpaces, //
                        (i1, i2) -> distance(i1, i2), true, "/home/mu/Downloads");
        Map<String, Integer> solution = redistributionLP.returnSolution();
        return solution;
    }

}
