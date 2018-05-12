/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.hungarian;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** @author Samuel J. Stauber */
class Graph {
    protected static final int UNASSIGNED = -1;
    // ---
    protected final int dim;
    protected final int rowDim;
    protected final int colDim;
    protected final double[][] costMatrix;
    private final double[][] costMatrixOriginal;

    protected int[] xMatch;
    protected int[] yMatch;

    protected double[] xLabel;
    protected double[] yLabel;

    protected final Set<Integer> freeX = new HashSet<>();
    protected final Set<Integer> freeY = new HashSet<>();

    private double optimalValue;

    public Graph(double[][] cm) {
        costMatrixOriginal = cm;
        rowDim = cm.length;
        colDim = cm[0].length;
        dim = Math.max(rowDim, colDim);

        costMatrix = new double[dim][];

        if (rowDim <= colDim) {
            for (int x = 0; x < dim; x++) { // dim == colDim
                costMatrix[x] = new double[dim];
                if (x < rowDim)
                    System.arraycopy(cm[x], 0, costMatrix[x], 0, cm[x].length);
            }
        } else {
            for (int y = 0; y < dim; y++) { // dim == rowDim
                costMatrix[y] = new double[dim];
                for (int x = 0; x < rowDim; x++) {
                    if (y < colDim)
                        costMatrix[y][x] = cm[x][y];
                }
            }
        }

        xMatch = new int[dim];
        Arrays.fill(xMatch, UNASSIGNED);

        yMatch = new int[dim];
        Arrays.fill(yMatch, UNASSIGNED);

    }

    protected void setInitialLabels() {
        for (int i = 0; i < dim; i++) {
            double min = Double.POSITIVE_INFINITY;
            for (int j = 0; j < dim; j++)
                if (costMatrix[i][j] < min)
                    min = costMatrix[i][j];
            xLabel[i] = min;
        }
    }

    public double getOptimalValue() {
        return optimalValue;
    }

    public int[] getResult() {
        int y;
        int[] resvec = xMatch;
        if (rowDim > colDim) {
            resvec = yMatch;
        }
        int[] result = new int[rowDim];
        for (int x = 0; x < rowDim; x++) {
            y = resvec[x];
            if (y >= colDim) {
                result[x] = UNASSIGNED;
                continue;
            }
            if (x >= rowDim)
                continue;
            result[x] = y;
        }
        return result;
    }

    public void saveOptimalValue() {
        double sum = 0;
        if (rowDim > colDim)
            for (int i = 0; i < rowDim; i++) {
                int x = yMatch[i];
                int y = i;
                if (x < colDim)
                    sum += costMatrixOriginal[y][x];
            }
        else
            for (int i = 0; i < rowDim; i++) {
                int x = i;
                int y = xMatch[i];
                sum += costMatrixOriginal[x][y];
            }

        optimalValue = sum;

    }

    protected void match(int i, int j) {
        xMatch[i] = j;
        yMatch[j] = i;
    }

    protected void setInitialMatching() {
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {

                if (costMatrix[i][j] == xLabel[i] && //
                        xMatch[i] == UNASSIGNED && yMatch[j] == UNASSIGNED) { // one condition superfluous
                    match(i, j);
                    break;
                }
            }
        }
    }
}