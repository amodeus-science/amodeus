/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;

/** Implementation of the "Vehicle Routing for Shared-Mobility Systems with Time-Varying Demand" presented in
 * K. Spieser, S. Samaranayake, and E. Frazzoli, 2016.
 * American Control Conference (ACC), Boston, MA, 2016, pp. 796-802.
 * 
 * The problem 5.1 is implemented as described in the paper in the simulation section. The number of vehicles is given
 * and the two cost parameters weightR for rebalancing cost and weightQ for waiting customers can be given as input with
 * the condition weightR+weightQ=1.
 * 
 * One improvement was done so that the problem remains feasible:
 * For the equation V_i(T_P) all rebalancing and customer-carrying vehicles to virtual station i are considered, even if
 * they are still on the way. Therefore the integration with w_{ji}(\tau,\sigma) is ignored in this case. */
class LPTimeVariant extends LPTimeVariantBase {
    private final static double AVERAGE_VEL = 30.0;
    // ---
    // map with variableIDs in problem set up and linkIDs of virtualNetwork
    private final Map<List<Integer>, Integer> fIDvarID = new HashMap<>();
    private final double weightQ;
    private final double weightR;

    /** @param virtualNetwork
     *            the virtual network (complete directed graph) on which the optimization is computed. */
    public LPTimeVariant(VirtualNetwork<Link> virtualNetwork, Network network, ScenarioOptions scenOptions, Tensor lambda) {
        super(virtualNetwork, network, lambda);
        weightQ = scenOptions.getLPWeightQ();
        weightR = scenOptions.getLPWeightR();

        GlobalAssert.that(weightQ + weightR == 1.0);
    }

    @Override
    public Tensor getTimeVS_ij() {
        // identify the travel times between the virtual stations (based on the paper with direct euclidean distance between virtual stations and velocity 30km/h)
        return LPUtils.getEuclideanTravelTimeBetweenVSCenters(virtualNetwork, AVERAGE_VEL);
    }

    @Override
    public Tensor getwAlpha_ij() {
        return timeVS_ij;
    }

    @Override
    public Tensor getwLambda_ij() {
        return timeVS_ij;
    }

    @Override
    public Tensor getVmin_i() {
        Tensor Vmin_i = Array.zeros(nvNodes);
        // set lower bound on available vehicles per virtual station to 0
        for (int i = 0; i < nvNodes; i++) {
            Vmin_i.set(RealScalar.of(1.0), i);
        }
        return Vmin_i;
    }

    @Override
    public int getRowTotal() {
        return nvNodes * nvNodes * timeSteps + 1;
    }

    @Override
    public int getColumnTotal() {
        return 2 * nvNodes * nvNodes * timeSteps - 2 * nvNodes * timeSteps + nvNodes;
    }

    @Override
    public void initColumnF_ij() {
        // optimization variable f_ij[k]
        for (int t = 0; t < timeSteps; t++) {
            for (int i = 0; i < nvNodes; ++i) {
                for (int j = 0; j < nvNodes; ++j) {
                    if (j == i)
                        continue;
                    columnId++;
                    // variable name and initialization
                    String varName = ("f" + "_" + i + "," + j + "[" + t + "]");
                    GLPK.glp_set_col_name(lp, columnId, varName);
                    GLPK.glp_set_col_kind(lp, columnId, GLPKConstants.GLP_CV);
                    GLPK.glp_set_col_bnds(lp, columnId, GLPKConstants.GLP_LO, 0.0, 0.0); // Lower bound: second number irrelevant
                    fIDvarID.put(Arrays.asList(t, i, j), columnId);
                }
            }
        }
        System.out.println("f_ij[k] done");
    }

    @Override
    public void initRowF_ij_k(SWIGTYPE_p_int ind, SWIGTYPE_p_double val) {
        // row variable F_ij[k]
        for (int t = 1; t < timeSteps; t++) {
            for (int i = 0; i < nvNodes; i++) {
                for (int j = 0; j < nvNodes; j++) {
                    if (i == j)
                        continue;
                    rowId++;
                    // set name
                    String varName = ("F" + "_" + i + "," + j + "[" + t + "]");
                    GLPK.glp_set_row_name(lp, rowId, varName);

                    // set upper bound
                    double upperBound = 0;
                    for (int k = 0; k < t; k++) {
                        upperBound += lambdaAbsolute_ij.Get(k, i, j).number().doubleValue();
                    }
                    GLPK.glp_set_row_bnds(lp, rowId, GLPKConstants.GLP_UP, 0.0, upperBound); // Upper bound: first number irrelevant

                    // set all coefficient entries of matrix A to zero first
                    for (int var = 1; var <= columnTotal; var++) {
                        GLPK.intArray_setitem(ind, var, var);
                        GLPK.doubleArray_setitem(val, var, 0.0);
                    }
                    for (int k = 0; k < t; k++) {
                        int index = fIDvarID.get(Arrays.asList(k, i, j));
                        GLPK.intArray_setitem(ind, index, index);
                        GLPK.doubleArray_setitem(val, index, 1);
                    }

                    // turn over the entries to GLPK
                    GLPK.glp_set_mat_row(lp, rowId, columnTotal, ind, val);
                }
            }
        }
        System.out.println("F_ij[k] done");
    }

    @Override
    public void initRowF_ij_K(SWIGTYPE_p_int ind, SWIGTYPE_p_double val) {
        // row variable F_ij[K]
        for (int i = 0; i < nvNodes; i++) {
            for (int j = 0; j < nvNodes; j++) {
                if (i == j)
                    continue;
                rowId++;
                // set name
                String varName = ("F" + "_" + i + "," + j + "[" + timeSteps + "]");
                GLPK.glp_set_row_name(lp, rowId, varName);

                // set fixed bound
                double fixedBound = 0;
                for (int k = 0; k < timeSteps; k++) {
                    fixedBound += lambdaAbsolute_ij.Get(k, i, j).number().doubleValue();
                }
                GLPK.glp_set_row_bnds(lp, rowId, GLPKConstants.GLP_FX, fixedBound, fixedBound);

                // set all coefficient entries of matrix A to zero first
                for (int var = 1; var <= columnTotal; var++) {
                    GLPK.intArray_setitem(ind, var, var);
                    GLPK.doubleArray_setitem(val, var, 0.0);
                }
                for (int k = 0; k < timeSteps; k++) {
                    int index = fIDvarID.get(Arrays.asList(k, i, j));
                    GLPK.intArray_setitem(ind, index, index);
                    GLPK.doubleArray_setitem(val, index, 1);
                }

                // turn over the entries to GLPK
                GLPK.glp_set_mat_row(lp, rowId, columnTotal, ind, val);
            }
        }
        System.out.println("F_ij[K] done");
    }

    @Override
    public void initRowV_i_k(SWIGTYPE_p_int ind, SWIGTYPE_p_double val) {
        // row variable V_i[k]
        for (int t = 1; t < timeSteps; t++) {
            for (int i = 0; i < nvNodes; i++) {
                rowId++;
                // set name
                String varName = ("V" + "_" + i + "[" + t + "]");
                GLPK.glp_set_row_name(lp, rowId, varName);

                // set lower bound
                GLPK.glp_set_row_bnds(lp, rowId, GLPKConstants.GLP_LO, 0.0, 0.0);

                // set all coefficient entries of matrix A to zero first
                for (int var = 1; var <= columnTotal; var++) {
                    GLPK.intArray_setitem(ind, var, var);
                    GLPK.doubleArray_setitem(val, var, 0.0);
                }

                // set V_i[0]
                int index = vIDvarID.get(Arrays.asList(i));
                GLPK.intArray_setitem(ind, index, index);
                GLPK.doubleArray_setitem(val, index, 1.0);

                // set f_ij[k] and alpha_ij[k]
                for (int k = 0; k < t; k++) {
                    for (int j = 0; j < nvNodes; j++) {
                        if (j == i)
                            continue;
                        index = fIDvarID.get(Arrays.asList(k, i, j));
                        GLPK.intArray_setitem(ind, index, index);
                        GLPK.doubleArray_setitem(val, index, -1);
                        index = alphaIDvarID.get(Arrays.asList(k, i, j));
                        GLPK.intArray_setitem(ind, index, index);
                        GLPK.doubleArray_setitem(val, index, -1);
                    }
                }
                // set f_ji[k] and alpha_ji[k]
                for (int k = 0; k < t; k++) {
                    for (int j = 0; j < nvNodes; j++) {
                        if (j == i)
                            continue;
                        // heaviside function, skip if heaviside gives 0
                        if ((t - k) * timeInterval - wLambda_ij.Get(j, i).number().doubleValue() > 0) {
                            index = fIDvarID.get(Arrays.asList(k, j, i));
                            GLPK.intArray_setitem(ind, index, index);
                            GLPK.doubleArray_setitem(val, index, 1);
                        }
                        if ((t - k) * timeInterval - wAlpha_ij.Get(j, i).number().doubleValue() > 0) {
                            index = alphaIDvarID.get(Arrays.asList(k, j, i));
                            GLPK.intArray_setitem(ind, index, index);
                            GLPK.doubleArray_setitem(val, index, 1);
                        }
                    }
                }

                // turn over the entries to GLPK
                GLPK.glp_set_mat_row(lp, rowId, columnTotal, ind, val);
            }
        }
        System.out.println("V_i[k] done");
    }

    @Override
    public void initRowV_i_K(SWIGTYPE_p_int ind, SWIGTYPE_p_double val) {
        // row variable V_i[K]
        for (int i = 0; i < nvNodes; i++) {
            // set name
            rowId++;
            String varName = ("V" + "_" + i + "[" + timeSteps + "]");
            GLPK.glp_set_row_name(lp, rowId, varName);

            // set fixed bound
            GLPK.glp_set_row_bnds(lp, rowId, GLPKConstants.GLP_FX, 0.0, 0.0);

            // set all coefficient entries of matrix A to zero first
            for (int var = 1; var <= columnTotal; var++) {
                GLPK.intArray_setitem(ind, var, var);
                GLPK.doubleArray_setitem(val, var, 0.0);
            }

            int index;
            // set f_ij[k] and alpha_ij[k]
            for (int k = 0; k < timeSteps; k++) {
                for (int j = 0; j < nvNodes; j++) {
                    if (j == i)
                        continue;
                    index = fIDvarID.get(Arrays.asList(k, i, j));
                    GLPK.intArray_setitem(ind, index, index);
                    GLPK.doubleArray_setitem(val, index, -1);
                    index = alphaIDvarID.get(Arrays.asList(k, i, j));
                    GLPK.intArray_setitem(ind, index, index);
                    GLPK.doubleArray_setitem(val, index, -1);
                }
            }
            // set f_ji[k] and alpha_ji[k]
            for (int k = 0; k < timeSteps; k++) {
                for (int j = 0; j < nvNodes; j++) {
                    if (j == i)
                        continue;
                    index = fIDvarID.get(Arrays.asList(k, j, i));
                    GLPK.intArray_setitem(ind, index, index);
                    GLPK.doubleArray_setitem(val, index, 1);
                    index = alphaIDvarID.get(Arrays.asList(k, j, i));
                    GLPK.intArray_setitem(ind, index, index);
                    GLPK.doubleArray_setitem(val, index, 1);
                }
            }

            // turn over the entries to GLPK
            GLPK.glp_set_mat_row(lp, rowId, columnTotal, ind, val);
        }
        System.out.println("V_i[K] done");
    }

    @Override
    public void initObjCr() {
        // set C_r
        for (int t = 0; t < timeSteps; t++) {
            for (int i = 0; i < nvNodes; i++) {
                for (int j = 0; j < nvNodes; j++) {
                    if (i == j)
                        continue;
                    int index = alphaIDvarID.get(Arrays.asList(t, i, j));
                    GLPK.glp_set_obj_coef(lp, index, weightR / DURATION * gamma_ij.Get(i, j).number().doubleValue());
                }
            }
        }
    }

    @Override
    public void initObjCq() {
        // set C_q (the lambdaij term can be ignored)
        for (int t = 0; t < timeSteps - 1; t++) {
            for (int i = 0; i < nvNodes; i++) {
                for (int j = 0; j < nvNodes; j++) {
                    if (i == j)
                        continue;
                    int index = fIDvarID.get(Arrays.asList(t, i, j));
                    GLPK.glp_set_obj_coef(lp, index, -weightQ * timeInterval / DURATION * (timeSteps - t - 1));
                }
            }
        }
    }

    @Override
    protected void readF_ij() {
        for (int t = 0; t < timeSteps; t++) {
            for (int i = 0; i < nvNodes; i++) {
                for (int j = 0; j < nvNodes; j++) {
                    if (i == j)
                        continue;
                    fAbsolute_ij.set(RealScalar.of(GLPK.glp_get_col_prim(lp, fIDvarID.get(Arrays.asList(t, i, j)))), t, i, j);
                }
            }
        }
        fAbsolute_ij = LPUtils.getRoundedRequireNonNegative(fAbsolute_ij);
        fRate_ij = fAbsolute_ij.divide(RealScalar.of(timeInterval));
    }
}
