/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.hungarian;

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
public class FastHungarianAlgorithm {
    private final EqGraph eq;

    public FastHungarianAlgorithm(double[][] costMatrix, double eps) {
        eq = new EqGraph(costMatrix, eps);
    }

    public FastHungarianAlgorithm(double[][] costMatrix) {
        this(costMatrix, StaticHelper.EPS_DEFAULT);
    }

    public final int[] execute() {
        while (!eq.isSolved()) {
            int x = eq.pickFreeX();
            int y = eq.addS(x);
            eq.augmentMatching(x, y);
        }
        return eq.getResult();
    }

    /** call only after {@link #execute()}
     * 
     * @return */
    public final double getOptimalValue() {
        eq.saveOptimalValue();
        return eq.getOptimalValue();
    }

}
