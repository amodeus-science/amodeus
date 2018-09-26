/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.FeedforwardFluidicRebalancingPolicy;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.Magnitude;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.alg.Dimensions;

/** Implementation of the LPTimeInvariant solver of
 * "Feedforward Fluidic Optimal Rebalancing Policy" presented in
 * Pavone, M., Smith, S.L., Frazzoli, E. and Rus, D., 2012.
 * Robotic load balancing for mobility-on-demand systems.
 * The International Journal of Robotics Research, 31(7), pp.839-854.
 * 
 * On page 845 on the right-hand side is the implemented algorithm shown.
 * 
 * https://github.com/idsc-frazzoli/amodeus/files/2290529/lptimeinvariant-impl.pdf
 * 
 * Should be used together with {@link FeedforwardFluidicRebalancingPolicy} */
public class LPTimeInvariant implements LPSolver {
    /** map with variableIDs in problem set up and linkIDs of virtualNetwork */
    private final Map<List<Integer>, Integer> alphaIDvarID = new HashMap<>();
    protected final Map<List<Integer>, Integer> vIDvarID = new HashMap<>();
    private final glp_smcp parm = new glp_smcp();
    private final int nvNodes;
    private final int rowTotal;
    private final int columnTotal;
    private final int timeSteps;
    private final int timeInterval;
    private final int numberVehicles;
    // ---
    private glp_prob lp;
    private Tensor lambdaAbsolute_ij;
    private Tensor lambdaRate_ij;
    private Tensor gamma_ij;
    private Tensor alphaAbsolute_ij;
    private Tensor alphaRate_ij;
    private int columnId;
    private int rowId;

    /** @param virtualNetwork
     *            the virtual network (complete directed graph) on which the optimization is computed. */
    public LPTimeInvariant(VirtualNetwork<Link> virtualNetwork, Tensor lambdaAbsolute_ij, int numberOfVehicles) {
        numberVehicles = numberOfVehicles;
        nvNodes = virtualNetwork.getvNodesCount();
        gamma_ij = LPUtils.getEuclideanTravelTimeBetweenVSCenters(virtualNetwork, LPUtils.AVERAGE_VEL);
        timeSteps = Dimensions.of(lambdaAbsolute_ij).get(0);
        timeInterval = Magnitude.SECOND.toInt(LPUtils.DURATION) / timeSteps;
        this.lambdaAbsolute_ij = LPUtils.getRoundedRequireNonNegative(lambdaAbsolute_ij);
        lambdaRate_ij = lambdaAbsolute_ij.divide(RealScalar.of(timeInterval));
        columnTotal = getColumnTotal();
        rowTotal = getRowTotal();

        if (virtualNetwork.getvLinksCount() != (nvNodes * nvNodes - nvNodes)) {
            System.err.println("These computations are only valid for a complete graph. Aborting.");
            GlobalAssert.that(false);
        }

        System.out.println("creating rebalancing time-invariant LP for system with " + nvNodes + " virtualNodes");
    }

//    public LPTimeInvariant(VirtualNetwork<Link> virtualNetwork, Tensor lambdaAbsolute_ij) {
//        this(virtualNetwork, lambdaAbsolute_ij, LPUtils.getNumberOfVehicles());
//    }

    /** initiate the linear program */
    @Override
    public void initiateLP() {
        alphaAbsolute_ij = Array.zeros(timeSteps, rowTotal, rowTotal);
    }

    @Override
    public void solveLP(boolean mute) {
        System.out.println("solving time-invariant LP");

        for (int k = 0; k < timeSteps; ++k) {
            initiateSubLP(k);
            solveSubLP(mute, k);
        }
    }

    private void initiateSubLP(int timeIndex) {
        try {
            lp = GLPK.glp_create_prob();
            System.out.println("Problem created for time index " + timeIndex);
            GLPK.glp_set_prob_name(lp, "Rebalancing Problem");

            // initiate COLUMN variables
            GLPK.glp_add_cols(lp, columnTotal);
            columnId = 0;

            initColumnAlpha_ij();

            GlobalAssert.that(columnTotal == columnId);

            // initiate auxiliary ROW variables
            GLPK.glp_add_rows(lp, rowTotal);
            rowId = 0;

            // Allocate memory NOTE: the first value in this array is not used as variables are counted 1,2,3,...,n*n
            SWIGTYPE_p_int ind = GLPK.new_intArray(columnTotal + 1);
            SWIGTYPE_p_double val = GLPK.new_doubleArray(columnTotal + 1);

            initRowDeltaV_i(ind, val, timeIndex);

            GlobalAssert.that(rowTotal == rowId);

            // Free memory
            GLPK.delete_intArray(ind);
            GLPK.delete_doubleArray(val);

            // OBJECTIVE vector
            GLPK.glp_set_obj_name(lp, "z");
            GLPK.glp_set_obj_dir(lp, GLPKConstants.GLP_MIN);

            initObj();

        } catch (

        GlpkException ex) {
            ex.printStackTrace();
        }
    }

    private void solveSubLP(boolean mute, int timeIndex) {
        GLPK.glp_term_out(mute ? GLPK.GLP_OFF : GLPK.GLP_ON);

        GLPK.glp_init_smcp(parm);
        int ret = GLPK.glp_simplex(lp, parm); // ret==0 indicates of the algorithm ran correctly
        GlobalAssert.that(ret == 0);
        int stat = GLPK.glp_get_status(lp);

        if (stat == GLPK.GLP_NOFEAS) {
            System.out.println("LP has found infeasible solution in time index " + timeIndex);
            closeLP();
            GlobalAssert.that(false);
        }

        if (stat != GLPK.GLP_OPT) {
            System.out.println("LP has found suboptimal in time index " + timeIndex);
            closeLP();
            GlobalAssert.that(false);
        }

        readAlpha_ij(timeIndex);

        closeLP();
    }

    /** closing the LP in order to release allocated memory */
    private void closeLP() {
        // release storage allocated for LP
        GLPK.glp_delete_prob(lp);
    }

    private void initColumnAlpha_ij() {
        // optimization variable alpha_ij[k]
        for (int i = 0; i < nvNodes; ++i) {
            for (int j = 0; j < nvNodes; ++j) {
                if (j == i)
                    continue;
                columnId++;
                // variable name and initialization
                String varName = ("alpha" + "_" + i + "," + j);
                GLPK.glp_set_col_name(lp, columnId, varName);
                GLPK.glp_set_col_kind(lp, columnId, GLPKConstants.GLP_CV);
                GLPK.glp_set_col_bnds(lp, columnId, GLPKConstants.GLP_LO, 0.0, 0.0);
                alphaIDvarID.put(Arrays.asList(i, j), columnId);
            }
        }
    }

    private void initRowDeltaV_i(SWIGTYPE_p_int ind, SWIGTYPE_p_double val, int timeIndex) {
        // row variable deltaV_i
        for (int i = 0; i < nvNodes; i++) {
            rowId++;

            double bound = 0;
            for (int j = 0; j < nvNodes; j++) {
                if (i == j)
                    continue;
                bound += lambdaAbsolute_ij.Get(timeIndex, j, i).number().doubleValue() - lambdaAbsolute_ij.Get(timeIndex, i, j).number().doubleValue();
            }
            // set name
            String varName = ("deltaV" + "_" + i);
            GLPK.glp_set_row_name(lp, rowId, varName);
            GLPK.glp_set_row_bnds(lp, rowId, GLPKConstants.GLP_FX, bound, bound);

            // set all coefficient entries of matrix A to zero first
            for (int var = 1; var <= columnTotal; var++) {
                GLPK.intArray_setitem(ind, var, var);
                GLPK.doubleArray_setitem(val, var, 0.0);
            }
            for (int j = 0; j < nvNodes; j++) {
                if (j == i)
                    continue;
                int indexSource = alphaIDvarID.get(Arrays.asList(i, j));
                GLPK.intArray_setitem(ind, indexSource, indexSource);
                GLPK.doubleArray_setitem(val, indexSource, 1);
                int indexSink = alphaIDvarID.get(Arrays.asList(j, i));
                GLPK.intArray_setitem(ind, indexSink, indexSink);
                GLPK.doubleArray_setitem(val, indexSink, -1);
            }

            // turn over the entries to GLPK
            GLPK.glp_set_mat_row(lp, rowId, columnTotal, ind, val);
        }
    }

    private void initObj() {
        for (int i = 0; i < nvNodes; i++) {
            for (int j = 0; j < nvNodes; j++) {
                if (i == j)
                    continue;
                int index = alphaIDvarID.get(Arrays.asList(i, j));
                GLPK.glp_set_obj_coef(lp, index, gamma_ij.Get(i, j).number().doubleValue());
            }
        }
    }

    private void readAlpha_ij(int timeIndex) {
        Tensor alphaAbsolute = Array.zeros(nvNodes, nvNodes);
        for (int i = 0; i < nvNodes; i++) {
            for (int j = 0; j < nvNodes; j++) {
                if (i == j)
                    continue;
                alphaAbsolute.set(RealScalar.of(GLPK.glp_get_col_prim(lp, alphaIDvarID.get(Arrays.asList(i, j)))), i, j);
            }
        }
        Tensor alphaAbsoluteRounded = LPUtils.getRoundedRequireNonNegative(alphaAbsolute);
        alphaAbsolute_ij.set(v -> alphaAbsoluteRounded, timeIndex);
        alphaRate_ij = alphaAbsolute_ij.divide(RealScalar.of(timeInterval));
    }

    /** writes the solution of the LP on the consoles */
    @Override
    public void writeLPSolution() {
        System.out.println("The LP solution is:");
        System.out.println("The absolute Rebalancing: " + alphaAbsolute_ij);
        System.out.println("The absolute Customer drives: " + getFAbsolute_ij());
        System.out.println("Initial vehicle distribution: " + getV0_i());
    }

    private int getColumnTotal() {
        return nvNodes * (nvNodes - 1);
    }

    private int getRowTotal() {
        return nvNodes;
    }

    @Override
    public Tensor getAlphaRate_ij() {
        return alphaRate_ij;
    }

    @Override
    public Tensor getAlphaAbsolute_ij() {
        return alphaAbsolute_ij;
    }

    @Override
    public Tensor getFRate_ij() {
        return lambdaRate_ij;
    }

    @Override
    public Tensor getFAbsolute_ij() {
        return lambdaAbsolute_ij;
    }

    @Override
    public Tensor getV0_i() {
        Scalar floor = RealScalar.of(numberVehicles / nvNodes); // floor(nV / nvNodes)
        return Tensors.vector(v -> floor, nvNodes);
    }

    @Override
    public int getTimeInterval() {
        return timeInterval;
    }

}
