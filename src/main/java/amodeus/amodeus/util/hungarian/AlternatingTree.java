/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.hungarian;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import amodeus.amodeus.util.math.GlobalAssert;

/** @author Samuel J. Stauber */
final class AlternatingTree {
    private static final int UNASSIGNED = -1;

    private final Nodes T;
    private final Set<Integer> S = new HashSet<>();
    private final Set<Integer> nlsMinusT = new HashSet<>();

    private final int[] escapeFromY;
    private final int[] escapeFromX;

    private final double[] xLabel;
    private final double[] yLabel;
    private final double[][] costMatrix;
    private final double eps;
    private final int[] yMatch;
    private final double[] alpha;
    private final int dim;

    public AlternatingTree(int dim, double[] xLabel, double[] yLabel, int[] yMatch, double[][] costMatrix, double eps) {
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        this.yMatch = yMatch;
        this.costMatrix = costMatrix;
        this.dim = dim;
        this.eps = eps;

        T = new Nodes(dim);

        escapeFromY = new int[dim];
        escapeFromX = new int[dim];
        alpha = new double[dim];

        resetEscape();
    }

    public void setAlpha(int xi) {
        clear();
        for (int yi = 0; yi < dim; yi++) {
            escapeFromY[yi] = xi;
            alpha[yi] = costMatrix[xi][yi] - xLabel[xi] - yLabel[yi];
        }
    }

    public void updateAlpha(int xi) { // Slows down (n x n)-Problem
        for (int yi : T.getNotNodes()) {
            double alpha = costMatrix[xi][yi] - xLabel[xi] - yLabel[yi];
            if (this.alpha[yi] > alpha) {
                this.alpha[yi] = alpha;
                escapeFromY[yi] = xi;
            }
            if (alpha == 0)
                nlsMinusT.add(yi);
        }
    }

    public int pickNlsMinusT(int xi) {
        double min = Double.POSITIVE_INFINITY;
        // int minY = UNASSIGNED;
        if (nlsMinusT.isEmpty()) {
            for (int yi : T.getNotNodes())
                if (alpha[yi] < min) {
                    // minY = yi;
                    min = alpha[yi];
                    if (Math.abs(min) <= eps)
                        nlsMinusT.add(yi);
                }
            if (eps < Math.abs(min))
                updateLabels(min);
        }
        min = Double.POSITIVE_INFINITY;
        int minY = UNASSIGNED;
        for (int yi : nlsMinusT) {
            if (alpha[yi] < min) {
                min = alpha[yi];
                minY = yi;
            }
            if (yMatch[yi] == UNASSIGNED) {
                nlsMinusT.remove(yi);
                return yi;
            }
        }
        return minY;
    }

    public int escapeFromX(int x) {
        return escapeFromX[x];
    }

    public int escapeFromY(int y) {
        return escapeFromY[y];
    }

    public int addT(int x, int y) {
        GlobalAssert.that(0 <= escapeFromY[y]);
        // if (escapeFromY[y] < 0) { // <- this condition was never true in any test
        // escapeFromY[y] = x;
        // }
        escapeFromX[yMatch[y]] = y;
        T.add(y);
        nlsMinusT.remove(y);
        return yMatch[y];
    }

    public void addS(int xi) {
        S.add(xi);
    }

    private int updateLabels(double deltaAlpha) { // Slows down (n x n)-Problem
        double min = Double.POSITIVE_INFINITY;
        int minY = UNASSIGNED;
        for (int x : S)
            xLabel[x] += deltaAlpha;
        for (int y : T.getNodes())
            yLabel[y] -= deltaAlpha;
        for (int y : T.getNotNodes()) {
            if (alpha[y] < min) {
                min = alpha[y];
                minY = y;
            }
            alpha[y] -= deltaAlpha;
            if (alpha[y] == 0)
                nlsMinusT.add(y);
        }
        return minY;
    }

    private void clear() {
        resetEscape();
        S.clear();
        T.clear();
        nlsMinusT.clear();
    }

    private void resetEscape() {
        Arrays.fill(escapeFromX, UNASSIGNED);
        Arrays.fill(escapeFromY, UNASSIGNED);
    }
}
