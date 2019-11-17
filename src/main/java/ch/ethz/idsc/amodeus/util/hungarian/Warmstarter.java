/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.hungarian;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;

import ch.ethz.matsim.av.passenger.AVRequest;

/** @author Samuel J. Stauber */
// TODO DEPLOY does this class have to be public? if yes, document purpose of class
public class Warmstarter {
    private static final int UNASSIGNED = -1;
    private static final double EPS = 1e-8;
    public static List<Id<DvrpVehicle>> lastTaxis = null;
    public static List<AVRequest> lastRequests = null;
    public static List<Id<DvrpVehicle>> thisTaxis = null;
    public static List<AVRequest> thisRequests = null;
    private static int[] lastMatching = null;

    private static int[] lastInitMatching;
    private final int[] newMatching;
    private static int[] yMatch;
    private static int[] xMatch;
    public boolean hasResult = false;
    public static boolean hasFailed = false;

    private double[] xLabel;
    private double[] yLabel;

    private final double[][] costMatrix;
    private final int dim, colDim, rowDim;

    public Warmstarter(double[][] costMatrix, int[] newMatching) { // For testing
        this.costMatrix = costMatrix;
        rowDim = costMatrix.length;
        colDim = costMatrix[0].length;
        dim = Math.max(rowDim, colDim);
        xLabel = new double[dim];
        yLabel = new double[dim];
        yMatch = new int[dim];
        xMatch = new int[dim];
        this.newMatching = newMatching;
        Arrays.fill(yMatch, UNASSIGNED);
        Arrays.fill(xMatch, UNASSIGNED);

        matchAndLabel();
        setLabelsForFreeJobs();
        hasResult = true;
    }

    public Warmstarter(double[][] costMatrix, List<Id<DvrpVehicle>> taxis, List<AVRequest> requests) {
        this.costMatrix = costMatrix;
        rowDim = costMatrix.length;
        colDim = costMatrix[0].length;
        dim = Math.max(rowDim, colDim);
        xLabel = new double[dim];
        yLabel = new double[dim];
        yMatch = new int[dim];
        xMatch = new int[dim];
        newMatching = new int[dim];
        Arrays.fill(newMatching, UNASSIGNED);
        Arrays.fill(yMatch, UNASSIGNED);
        Arrays.fill(xMatch, UNASSIGNED);

        if (readyForWarmstart()) {
            setNewMatching(taxis, requests);
            matchAndLabel();
            setLabelsForFreeJobs();
            hasResult = true;
        }
        lastTaxis = taxis;
        lastRequests = requests;
    }

    public double[] getXLabels() {
        return xLabel;
    }

    public double[] getYLabels() {
        return yLabel;
    }

    public int[] getXMatch() {
        return xMatch;
    }

    public int[] getYMatch() {
        return yMatch;
    }

    private void matchAndLabel() {
        double[] oldX;
        double[] oldY;
        for (int x = 0; x < rowDim; x++)
            if (newMatching[x] > UNASSIGNED) {
                oldX = Arrays.copyOf(xLabel, xLabel.length);
                oldY = Arrays.copyOf(yLabel, yLabel.length);
                yMatch[newMatching[x]] = x;
                if (isMatchPossible(x, newMatching[x])) {
                    xMatch[x] = newMatching[x];
                    yMatch[xMatch[x]] = x;
                } else {
                    xLabel = oldX;
                    yLabel = oldY;
                    yMatch[newMatching[x]] = UNASSIGNED;
                }
            }
    }

    private boolean isMatchPossible(int x, int y) {
        double cost = getCost(x, y);
        xLabel[x] = cost - yLabel[y];
        return updateInY(x);
    }

    private boolean updateInY(int x) {
        double xL = xLabel[x];
        for (int y = 0; y < colDim; y++)
            if (xL + yLabel[y] - getCost(x, y) > 1e-8) {
                yLabel[y] = getCost(x, y) - xL;
                if (yMatch[y] == UNASSIGNED)
                    continue;
                return false;
            }
        return true;
    }

    private double getCost(int x, int y) {
        if (x >= rowDim || y >= colDim)
            return 0;
        return costMatrix[x][y];
    }

    private void setNewMatching(List<Id<DvrpVehicle>> actTaxi, List<AVRequest> actRequest) {
        Id<DvrpVehicle> tmpTaxi = null;
        AVRequest tmpReq = null;
        int tmpTaxiId = UNASSIGNED;
        int last = UNASSIGNED;
        // List<Integer> taxiIDmatched = new ArrayList<>();
        // List<Integer> reqIDmatched = new ArrayList<>();

        for (int i = 0; i < lastTaxis.size(); i++) {
            tmpTaxi = lastTaxis.get(i);
            last = lastMatching[i];
            if (last >= lastRequests.size()) {
                continue;
            }
            tmpReq = lastRequests.get(last);
            if (!actTaxi.contains(tmpTaxi) || !actRequest.contains(tmpReq))
                continue; // Request is gone or taxi is gone or was matched to dummy job
            for (int j = 0; j < actTaxi.size(); j++) { // Find position of taxi in vector
                if (actTaxi.get(j).equals(tmpTaxi)) {
                    tmpTaxiId = j;
                    break;
                }
            }
            for (int j = 0; j < actRequest.size(); j++) {
                if (actRequest.get(j).equals(tmpReq)) {
                    // taxiIDmatched.add(tmpTaxiId);
                    // reqIDmatched.add(j);
                    newMatching[tmpTaxiId] = j; // Find position of corresp request in vector
                    break;
                }
            }
        }
        lastRequests = actRequest; // For next warmstart
        lastTaxis = actTaxi;
    }

    private void setLabelsForFreeJobs() {
        double min;
        boolean foundDummy;
        int dummy = UNASSIGNED;
        for (int i = 0; i < newMatching.length; i++)
            if (xMatch[i] == UNASSIGNED) {
                min = Double.POSITIVE_INFINITY;
                foundDummy = false;
                for (int y = 0; y < dim; y++) {
                    if (y >= colDim && yMatch[y] < 0 && yLabel[y] >= xLabel[i] && !foundDummy) {
                        foundDummy = true;
                        dummy = y;
                    }
                    if (min + yLabel[y] > getCost(i, y))
                        min = getCost(i, y) - yLabel[y];
                }
                xLabel[i] = min;
                if (foundDummy) {
                    yLabel[dummy] = -min;
                    xMatch[i] = dummy;
                    yMatch[dummy] = i;
                }
            }
    }

    public boolean isValidLabeling() {
        boolean valid = true;
        for (int x = 0; x < dim; x++)
            if (xMatch[x] != UNASSIGNED) {
                if (Math.abs(xLabel[x] + yLabel[xMatch[x]] - getCost(x, xMatch[x])) > EPS) {
                    valid = false;
                    System.out.println("Bad matching label (" + x + "," + xMatch[x] + ")");
                }
                for (int y = 0; y < dim; y++)
                    if (xLabel[x] + yLabel[y] - getCost(x, y) > EPS && yMatch[y] == UNASSIGNED) {
                        valid = false;
                        System.out.println("Bad label (" + x + "," + y + ")");
                    }
            }
        if (valid)
            System.out.println("Labels are ok");
        return valid;
    }

    public static void setLastData(List<Id<DvrpVehicle>> lastTaxi, List<AVRequest> lastRequest) {
        lastTaxis = lastTaxi;
        lastRequests = lastRequest;
    }

    public static void setLastMatching(int[] xMatch) {
        lastMatching = xMatch;
    }

    public boolean readyForWarmstart() {
        return Objects.nonNull(lastRequests);
    }

    public int[] getXMatchBefore() {
        return lastInitMatching;
    }
}
