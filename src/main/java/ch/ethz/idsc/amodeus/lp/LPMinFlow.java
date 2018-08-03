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

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualLink;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.red.Total;
import ch.ethz.idsc.tensor.sca.Sign;

class LPMinFlow {
    private final static double AVERAGE_VEL = 30.0;
    // ---
    // map with variableIDs in problem set up and linkIDs of virtualNetwork
    private final Map<List<Integer>, Integer> alphaIDvarID = new HashMap<>();
    private final glp_smcp parm = new glp_smcp();
    private final VirtualNetwork<Link> virtualNetwork;
    private final int nvNodes;
    private final int rowTotal;
    private final int columnTotal;
    // ---
    private glp_prob lp;
    private Tensor gamma_ij;
    private Tensor alphaAbsolute_ij;
    private int columnId;
    private int rowId;

    /** @param virtualNetwork
     *            the virtual network (not necessarily complete graph) on which the optimization is computed. */
    public LPMinFlow(VirtualNetwork<Link> virtualNetwork) {
        this.virtualNetwork = virtualNetwork;
        nvNodes = virtualNetwork.getvNodesCount();
        columnTotal = virtualNetwork.getvLinksCount();
        rowTotal = virtualNetwork.getvNodesCount();

        gamma_ij = LPUtils.getEuclideanTravelTimeBetweenVSCenters(virtualNetwork, AVERAGE_VEL);
        alphaAbsolute_ij = Array.zeros(nvNodes, nvNodes);

        System.out.println("creating min flow LP for system with " + rowTotal + " virtualNodes and " + columnTotal + " virtualLinks");
    }

    /** initiate the linear program */
    public void initiateLP() {
        try {
            lp = GLPK.glp_create_prob();
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

            initRowV_i(ind, val);

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

    public void solveLP(boolean mute, Tensor minFlow) {
        // use minFlow as lower bound
        GlobalAssert.that(minFlow.length() == nvNodes);
        minFlow = LPUtils.getRounded(minFlow);
        // the problem is only feasible when the sum of minFlow is less or equal zero
        GlobalAssert.that(Sign.isNegativeOrZero(Total.of(minFlow).Get()));
        for (int i = 0; i < nvNodes; ++i) {
            GLPK.glp_set_row_bnds(lp, i + 1, GLPK.GLP_LO, minFlow.Get(i).number().doubleValue(), 0.0); // Lower bound: second number irrelevant
        }

        GLPK.glp_term_out(mute ? GLPK.GLP_OFF : GLPK.GLP_ON);

        GLPK.glp_init_smcp(parm);
        int ret = GLPK.glp_simplex(lp, parm); // ret==0 indicates of the algorithm ran correctly
        GlobalAssert.that(ret == 0);
        int stat = GLPK.glp_get_status(lp);

        if (stat == GLPK.GLP_NOFEAS) {
            System.out.println("LP has found infeasible solution");
            closeLP();
            GlobalAssert.that(false);
        }

        if (stat != GLPK.GLP_OPT) {
            System.out.println("LP has found suboptimal");
            closeLP();
            GlobalAssert.that(false);
        }
        readAlpha_ij();
    }

    /** closing the LP in order to release allocated memory */
    public void closeLP() {
        // release storage allocated for LP
        GLPK.glp_delete_prob(lp);
    }

    private void initColumnAlpha_ij() {
        // optimization variable alpha_ij
        for (VirtualLink<Link> link : virtualNetwork.getVirtualLinks()) {
            int i = link.getFrom().getIndex();
            int j = link.getTo().getIndex();

            columnId++;
            // variable name and initialization
            String varName = ("alpha" + "_" + i + "," + j);
            GLPK.glp_set_col_name(lp, columnId, varName);
            GLPK.glp_set_col_kind(lp, columnId, GLPKConstants.GLP_CV);
            GLPK.glp_set_col_bnds(lp, columnId, GLPKConstants.GLP_LO, 0.0, 0.0); // Lower bound: second number irrelevant
            alphaIDvarID.put(Arrays.asList(i, j), columnId);
        }
        // System.out.println("alpha_ij done");
    }

    private void initRowV_i(SWIGTYPE_p_int ind, SWIGTYPE_p_double val) {
        // row variable V_i
        for (int i = 0; i < nvNodes; i++) {
            rowId++;

            // set name
            String varName = ("V" + "_" + i);
            GLPK.glp_set_row_name(lp, rowId, varName);

            // set all coefficient entries of matrix A to zero first
            for (int var = 1; var <= columnTotal; var++) {
                GLPK.intArray_setitem(ind, var, var);
                GLPK.doubleArray_setitem(val, var, 0.0);
            }
            for (int j = 0; j < nvNodes; j++) {
                if (alphaIDvarID.containsKey(Arrays.asList(i, j))) {
                    int indexSource = alphaIDvarID.get(Arrays.asList(i, j));
                    GLPK.intArray_setitem(ind, indexSource, indexSource);
                    GLPK.doubleArray_setitem(val, indexSource, -1);
                }
                if (alphaIDvarID.containsKey(Arrays.asList(j, i))) {
                    int indexSink = alphaIDvarID.get(Arrays.asList(j, i));
                    GLPK.intArray_setitem(ind, indexSink, indexSink);
                    GLPK.doubleArray_setitem(val, indexSink, 1);
                }
            }

            // turn over the entries to GLPK
            GLPK.glp_set_mat_row(lp, rowId, columnTotal, ind, val);
        }
        // System.out.println("V_i done");
    }

    private void initObj() {
        for (VirtualLink<Link> link : virtualNetwork.getVirtualLinks()) {
            int i = link.getFrom().getIndex();
            int j = link.getTo().getIndex();
            int index = alphaIDvarID.get(Arrays.asList(i, j));
            GLPK.glp_set_obj_coef(lp, index, gamma_ij.Get(i, j).number().doubleValue());
        }
    }

    private void readAlpha_ij() {
        for (VirtualLink<Link> link : virtualNetwork.getVirtualLinks()) {
            int i = link.getFrom().getIndex();
            int j = link.getTo().getIndex();
            int index = alphaIDvarID.get(Arrays.asList(i, j));
            alphaAbsolute_ij.set(RealScalar.of(GLPK.glp_get_col_prim(lp, index)), i, j);
        }
        alphaAbsolute_ij = LPUtils.getRoundedRequireNonNegative(alphaAbsolute_ij);
    }

    /** writes the solution of the LP on the consoles */
    public void writeLPSolution() {
        System.out.println("The LP solution is:");
        System.out.println("The absolute Rebalancing: " + alphaAbsolute_ij);
    }

    public Tensor getAlphaAbsolute_ij() {
        return alphaAbsolute_ij;
    }
}
