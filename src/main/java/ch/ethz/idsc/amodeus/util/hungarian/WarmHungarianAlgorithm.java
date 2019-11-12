/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.hungarian;

import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;

import ch.ethz.matsim.av.passenger.AVRequest;

/** This program executes the Hungarian Algorithm in order to solve a bipartite
 * matching problem . An entry [i][j] of the double[n][m]-input-array represents
 * the cost of matching worker i to job j. An entry [i] of the int[n]-output-
 * array stores the best job j that was assigned to worker i. If there is no
 * job for a worker, i.e. j>i, the entry in the output-array will read -1.
 * 
 * The resulting matching will have minimum cost and therefore is an optimum.
 * All entries in the output array are unique.
 * 
 * @author Samuel J. Stauber */
public class WarmHungarianAlgorithm {
    private final EqGraph eq;
    public static Warmstarter w;
    public static boolean warmstart;
    public static boolean labelWasFeasible = true;

    public WarmHungarianAlgorithm(double[][] costMatrix, double eps) {
        eq = new EqGraph(costMatrix, eps);
        warmstart = false;
    }

    public WarmHungarianAlgorithm(double[][] costMatrix, double eps, List<Id<DvrpVehicle>> taxis, List<AVRequest> requests) {
        w = new Warmstarter(costMatrix, taxis, requests);
        warmstart = true;

        eq = w.hasResult //
                ? new EqGraph(costMatrix, eps, w) //
                : new EqGraph(costMatrix, eps);
    }

    public WarmHungarianAlgorithm(double[][] costMatrix, double eps, Warmstarter w) {
        eq = new EqGraph(costMatrix, eps, w);
    }

    public final double getOptimalValue() {
        eq.saveOptimalValue();
        return eq.getOptimalValue();
    }

    public final int[] execute() {
        int x;
        int y;
        while (!eq.isSolved()) {
            x = eq.pickFreeX();
            y = eq.addS(x);
            eq.augmentMatching(x, y);
        }
        // int[] result = eq.getResult();// bad
        if (warmstart) {
            int[] result = eq.getWarmMatch();// better?
            Warmstarter.setLastMatching(result);
        }
        return eq.getResult();
    }
}
