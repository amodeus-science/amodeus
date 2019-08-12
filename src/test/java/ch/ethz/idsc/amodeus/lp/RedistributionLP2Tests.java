package ch.ethz.idsc.amodeus.lp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

public class RedistributionLP2Tests {

    private static final int n1 = 2;
    private static final int n2 = 5;
    private static final Random random = new Random(10);
    private static long seed = 123;

    @Test
    public void test() {

        /** roboTaxi must leave link 1 */
        Map<Integer, Set<String>> agentsToGo = new HashMap<>();
        for (int i = 0; i < n1; ++i) {
            HashSet<String> agents = new HashSet<>();
            agents.add("agent_A_" + i);
            agents.add("agent_B_" + i);
            agentsToGo.put(i, agents);
        }

        /** free spots available on link 2 */
        Map<Integer, Long> freeSpaces = new HashMap<>();
        for (int i = n1; i < n1 + n2; ++i) {
            freeSpaces.put(i, (long) 1);
        }

        /** solve it */
        RedistributionProblemSolver<Integer, String> redistributionLP = //
                new RedistributionProblemSolver<>(agentsToGo, freeSpaces, //
                        (i1, i2) -> distance(i1, i2), true, "/home/mu/Downloads");
        Map<String, Integer> solution = redistributionLP.returnSolution();
        for (Entry<String, Integer> entry : solution.entrySet()) {
            System.out.println(entry.getKey() + ",  " + entry.getValue());
        }
        // Assert.assertTrue(solution.get("agent_0").equals(4));
        // Assert.assertTrue(solution.get("agent_1").equals(5));
    }

    private static double distance(Integer i1, Integer i2) {
        if (i1 == 0 && i2 == n1)
            return 100;
        return 2;
        // Random random = new Random(seed);
        // return i1 * random.nextDouble() + i2 * random.nextDouble();
    }

}
