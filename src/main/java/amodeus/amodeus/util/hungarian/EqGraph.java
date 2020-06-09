/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.hungarian;

/** @author Samuel J. Stauber */
public final class EqGraph extends Graph {
    private final AlternatingTree at;

    public EqGraph(double[][] cm, double eps) {
        super(cm);
        xLabel = new double[dim];
        yLabel = new double[dim];
        at = new AlternatingTree(dim, xLabel, yLabel, yMatch, costMatrix, eps);
        setInitialLabels();
        setInitialMatching();
        initializeFreeNodes();
    }

    public EqGraph(double[][] cm, double eps, Warmstarter w) {
        super(cm);
        if (colDim >= rowDim) {
            xLabel = w.getXLabels();
            yLabel = w.getYLabels();
            xMatch = w.getXMatch();
            yMatch = w.getYMatch();
        } else {
            xLabel = w.getYLabels();
            yLabel = w.getXLabels();
            xMatch = w.getYMatch();
            yMatch = w.getXMatch();
        }

        at = new AlternatingTree(dim, xLabel, yLabel, yMatch, costMatrix, eps);

        initializeFreeNodes();
        // System.out.println("Found "+(xMatch.length-freeX.size())+"/"+xMatch.length+" (Nr of free X: "+freeX.size()+" )");
        if (freeX.size() != freeY.size()) {
            System.out.println("BUG");
            throw new RuntimeException("size mismatch");
        }
    }

    public int addS(int xi) {
        while (true) {
            int yi = at.pickNlsMinusT(xi);
            if (yMatch[yi] == UNASSIGNED)
                return yi;
            xi = at.addT(xi, yi);
            at.addS(xi);
            at.updateAlpha(xi);
        }
    }

    public void augmentMatching(int stoppingX, int startingY) {
        int x;
        int y = startingY;
        do {
            x = at.escapeFromY(y);
            match(x, y);
            y = at.escapeFromX(x);
        } while (x != stoppingX);
        freeX.remove(stoppingX);
        freeY.remove(startingY);
    }

    public boolean isSolved() {
        return freeX.isEmpty();
    }

    public int pickFreeX() {
        int x = freeX.stream().findFirst().orElse(UNASSIGNED);
        at.setAlpha(x);
        at.addS(x);
        return x;
    }

    private void initializeFreeNodes() {
        for (int i = 0; i < dim; i++) {
            if (xMatch[i] < 0)
                freeX.add(i);
            if (yMatch[i] < 0)
                freeY.add(i);
        }
    }

    public int[] getWarmMatch() {
        return rowDim > colDim //
                ? yMatch //
                : xMatch;
    }
}
