/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.io.Timing;
import ch.ethz.idsc.tensor.red.Norm;
import ch.ethz.idsc.tensor.sca.Chop;

public class RedistributionIntegralityTests {

    private static Random random;

    private static final int l1 = 1;
    private static final int l2 = 1;

    @Test
    public void test1() {
        for (int n1 = 1; n1 < 20; ++n1)
            for (int n2 = n1 + 1; n2 < 20; ++n2)
                for (int l2 = 1; l2 < 3; ++l2)
                    for (long seed = 1; seed < 3; ++seed) {
                        random = new Random(seed);
                        Map<String, Map<String, Double>> solution_LP = localSolverLP(n1, n2);
                        random = new Random(seed);
                        Map<String, Map<String, Integer>> solutionILP = localSolver(n1, n2);

                        Timing timing1 = Timing.started();
                        Tensor milpsol = Tensors.empty();
                        solutionILP.values().forEach(m -> m.values().stream().forEach(i -> {
                            milpsol.append(RealScalar.of(i));
                        }));
                        timing1.stop();

                        Timing timing2 = Timing.started();
                        Tensor lpsol = Tensors.empty();
                        solution_LP.values().forEach(m -> m.values().stream().forEach(d -> {
                            lpsol.append(RealScalar.of(d));
                        }));
                        timing2.stop();

                        // System.out.println(milpsol);
                        // System.out.println(lpsol);
                        // System.out.println("The norm of the difference:");
                        // System.out.println(Norm._2.of(milpsol.subtract(lpsol)));
                        // System.out.println("Time MILP:\t" + timeMILP);
                        // System.out.println("Time LP:\t" + timeILP);

                        Scalar normFull = Norm._2.of(milpsol.subtract(lpsol));
                        Scalar normChop = Chop._10.apply(normFull);
                        System.out.println("Normfull: \t" + normFull);
                        System.out.println("Normchop: \t" + normChop);
                        Assert.assertEquals(normFull, normChop);
                    }
    }

    /** helper functions */
    private static double distance(String i1, String i2) {
        return random.nextDouble();
    }

    private static Map<String, Map<String, Double>> localSolverLP(int n1, int n2) {
        /** roboTaxi must leave link 1 */
        Map<String, Integer> agentsToGo = new HashMap<>();
        for (int i = 1; i <= n1; ++i)
            agentsToGo.put("o" + i, l1);

        /** free spots available on link 2 */
        Map<String, Integer> freeSpaces = new HashMap<>();
        for (int i = 1; i <= n2; ++i)
            freeSpaces.put("d" + i, l2);

        /** solve it */
        RedistributionProblemSolver<String> redistributionLP = //
                new RedistributionProblemSolver<>(agentsToGo, freeSpaces, //
                        RedistributionIntegralityTests::distance, s -> s, false, "");
        return redistributionLP.returnDoubleSolution();
    }

    private static Map<String, Map<String, Integer>> localSolver(int n1, int n2) {
        /** roboTaxi must leave link 1 */
        Map<String, Integer> agentsToGo = new HashMap<>();
        for (int i = 1; i <= n1; ++i)
            agentsToGo.put("o" + i, l1);

        /** free spots available on link 2 */
        Map<String, Integer> freeSpaces = new HashMap<>();
        for (int i = 1; i <= n2; ++i)
            freeSpaces.put("d" + i, l2);

        /** solve it */
        RedistributionProblemSolverMILP<String> redistributionLP = //
                new RedistributionProblemSolverMILP<>(agentsToGo, freeSpaces, //
                        RedistributionIntegralityTests::distance, s -> s, false, "");
        return redistributionLP.returnSolution();
    }

}
