/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.alg.Rescale;
import ch.ethz.idsc.tensor.red.Total;

/* package */ class GlobalBipartiteHelperILP<T> {

    private final GLPKAssignmentSolverBetter solver;
    private Map<RoboTaxi, T> previousAssignment = new HashMap<>();

    public GlobalBipartiteHelperILP(GLPKAssignmentSolverBetter solver) {
        this.solver = solver;
    }

    public Map<RoboTaxi, T> genericMatch(Collection<RoboTaxi> roboTaxis, Collection<T> linkObjects, //
            Function<T, Link> linkOfT, GlobalBipartiteCost weight) {
        /** storage in {@link List} as {@link Collection} does not guarantee order */
        final List<RoboTaxi> orderedRoboTaxis = new ArrayList<>(roboTaxis);
        final List<T> ordered_linkObjects = new ArrayList<>(linkObjects);

        /** setup cost matrix */
        final int n = orderedRoboTaxis.size(); // workers
        final int m = ordered_linkObjects.size(); // jobs
        final double[][] costMatrix = new double[n][m];

        /** cost of assigning vehicle i to dest j, i.e. distance from vehicle i to destination j */
        Tensor lastSolution = Array.zeros(n, m);
        int i = 0;
        for (RoboTaxi roboTaxi : orderedRoboTaxis) {
            /** cost of current assignments */
            int j = 0;
            for (T t : ordered_linkObjects)
                costMatrix[i][j++] = weight.between(roboTaxi, linkOfT.apply(t));
            ++i;

            /** possible previous assignments */
            if (previousAssignment.containsKey(roboTaxi)) {
                PassengerRequest previous = (PassengerRequest) previousAssignment.get(roboTaxi);
                int k = 0;
                for (T t : ordered_linkObjects) {
                    if (t.equals(previous))
                        break;
                    ++k;
                }
                lastSolution.set(RealScalar.ONE, i, k);
            }
        }

        // TODO @clruch add some explanation of your solution format
        Tensor tensorCost = Tensors.matrixDouble(costMatrix);

        // previous version, did work and provided same performance as conventional GBM dispatcher
        // Tensor solution = solver.solve(Rescale.of(tensorCost));
        Tensor solution = solver.solveAdvanced(Rescale.of(tensorCost), lastSolution);
        Map<RoboTaxi, T> map = new HashMap<>();

        for (int k = 0; k < n; ++k) {
            Tensor t = solution.get(k);
            // ExactTensorQ.require(solution);
            Scalar tSum = (Scalar) Total.of(t);

            // either the robotaxi at line t is assigned or not, sum must be 0 or 1, no
            // other value admissible
            GlobalAssert.that(tSum.equals(RealScalar.ONE) || tSum.equals(RealScalar.ZERO));
            if (tSum.equals(RealScalar.ONE)) { // assignment was made by algorithm
                for (int l = 0; l < t.length(); ++l)
                    if (t.Get(l).equals(RealScalar.ONE)) {
                        map.put(orderedRoboTaxis.get(k), ordered_linkObjects.get(l));
                        break;
                    }
            }
        }
        previousAssignment = map;
        return map;
    }
}
