/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ class ParkingLPSolver<T> {

    private final Map<T, Set<RoboTaxi>> taxisToGo;
    private final Map<T, Long> freeSpacesToGo;
    private final int totalTaxis;
    private final int totalSpaces;
    private final List<T> taxiLinks;
    private final List<T> freeSpacesLinks;
    private final Map<RoboTaxi, T> result = new HashMap<>();
    private glp_prob lp;
    private glp_iocp parm;

    public ParkingLPSolver(Map<T, Set<RoboTaxi>> taxisToGo, Map<T, Long> freeSpacesToGo, //
            BiFunction<T, T, Double> distanceFunction) {

        // DistanceFunction distanceFunction) {
        /** copying input arguments */
        this.taxisToGo = taxisToGo;
        this.freeSpacesToGo = freeSpacesToGo;
        totalTaxis = taxisToGo.size();
        totalSpaces = freeSpacesToGo.size();
        System.out.println("total taxi links: " + totalTaxis);
        System.out.println("total space links: " + totalSpaces);
        taxiLinks = new ArrayList<>(taxisToGo.keySet());
        freeSpacesLinks = new ArrayList<>(freeSpacesToGo.keySet());
        System.out.println("starting to define lp");
        Long time = System.currentTimeMillis();
        // if there are not enough parking spaces, the problem is infeasible
        // an optimal solution is not defined.
        GlobalAssert.that(totalTaxis >= totalSpaces);

        /** definition of LP */
        this.lp = defineLP(distanceFunction);
        Long time2 = System.currentTimeMillis();
        Long elapsed = time2 - time;
        System.out.println("time to define:            " + elapsed.toString());

        /** solving LP */
        solveLP(true);
        Long time3 = System.currentTimeMillis();
        elapsed = time3 - time2;
        System.out.println("time to solve:             " + elapsed.toString());

        /** extracting solution and removing */
        extractSolution();
        GLPK.glp_delete_prob(lp);
        Long time4 = System.currentTimeMillis();
        elapsed = time4 - time3;
        System.out.println("time to extract solution:  " + elapsed.toString());
    }

    public Map<RoboTaxi, T> returnSolution() {
        return result;
    }

    /** INTEGER LINEAR PROGRAM
     * min sum_(i in taxiLinks) sum_(j in freeSpacesLinks) cost_ij * x_ij
     * s.t.
     * (c1) x_ij >= 0
     * (c2) sum_(i in taxiLinks) x_ij <= freeSpaces at j
     * (c3) sum_(j in freeSpacesLinks) x_ij = numberOfTaxis at i */
    // private glp_prob defineLP(DistanceFunction distanceFunction) {
    private glp_prob defineLP(BiFunction<T, T, Double> distanceFunction) {
        try {
            lp = GLPK.glp_create_prob();

            // set up optimization variables
            GLPK.glp_add_cols(lp, totalTaxis * totalSpaces);

            // create all optimization variables with objective weight and lower bound constraint
            Integer j = 1;
            for (T taxiLink : taxiLinks) {
                // if (j % 10 == 0)
                // System.out.println("j (1): " + j);
                for (T freeSpaceLink : freeSpacesLinks) {
                    GLPK.glp_set_col_kind(lp, j, GLPKConstants.GLP_IV);
                    GLPK.glp_set_col_bnds(lp, j, GLPKConstants.GLP_LO, 0, 0);
                    GLPK.glp_set_obj_coef(lp, j, //
                            distanceFunction.apply(taxiLink, freeSpaceLink));
                    /** TRACKING FOR LATER ASSIGNMENT */
                    Map<T, T> linkMap = new HashMap<>();
                    linkMap.put(taxiLink, freeSpaceLink);
                    j++;
                }
            }
            GlobalAssert.that(j != (totalTaxis * totalSpaces));

            // create equality constraint
            j = 1;
            for (T taxiLink : taxiLinks) {
                // if (j % 10 == 0)
                // System.out.println("j (2): " + j);
                GLPK.glp_add_rows(lp, 1);
                GLPK.glp_set_row_bnds(lp, j, GLPKConstants.GLP_FX, taxisToGo.get(taxiLink).size(), taxisToGo.get(taxiLink).size());
                SWIGTYPE_p_int ind = GLPK.new_intArray((totalSpaces * totalTaxis) + 1);
                SWIGTYPE_p_double val = GLPK.new_doubleArray((totalSpaces * totalTaxis) + 1);
                for (int k = 1; k <= (totalSpaces * totalTaxis); k++) {
                    GLPK.intArray_setitem(ind, k, k);
                    if ((k <= (j * totalSpaces)) & (k > ((j - 1) * totalSpaces))) {
                        GLPK.doubleArray_setitem(val, k, 1);
                    } else {
                        GLPK.doubleArray_setitem(val, k, 0);
                    }
                }
                GLPK.glp_set_mat_row(lp, j, totalSpaces * totalTaxis, ind, val);
                GLPK.delete_intArray(ind);
                GLPK.delete_doubleArray(val);
                j++;
            }
            GlobalAssert.that(j != totalTaxis);

            // create inequality constraint
            Integer l = 1;
            for (T freeSpaceLink : freeSpacesLinks) {
                if (l % 100 == 0)
                    System.out.println("l (3): " + l);
                GLPK.glp_add_rows(lp, 1);
                GLPK.glp_set_row_bnds(lp, j, GLPKConstants.GLP_UP, freeSpacesToGo.get(freeSpaceLink), freeSpacesToGo.get(freeSpaceLink));
                SWIGTYPE_p_int ind = GLPK.new_intArray((totalSpaces * totalTaxis) + 1);
                SWIGTYPE_p_double val = GLPK.new_doubleArray((totalSpaces * totalTaxis) + 1);
                for (int k = 1; k <= (totalSpaces * totalTaxis); k++) {
                    GLPK.intArray_setitem(ind, k, k);
                    if (Math.floorMod(k - 1, totalSpaces) == 0) {
                        GLPK.doubleArray_setitem(val, k, 1);
                    } else {
                        GLPK.doubleArray_setitem(val, k, 0);
                    }
                }

                GLPK.glp_set_mat_row(lp, j, totalSpaces * totalTaxis, ind, val);
                GLPK.delete_intArray(ind);
                GLPK.delete_doubleArray(val);
                l++;
                j++;
            }
            GlobalAssert.that(j != (totalTaxis + totalSpaces));
            GlobalAssert.that(l != totalSpaces);

        } catch (GlpkException e) {
            e.printStackTrace();
        }
        return lp;
    }

    private void solveLP(boolean mute) {
        parm = new glp_iocp();
        GLPK.glp_init_iocp(parm);
        parm.setPresolve(GLPK.GLP_ON);
        int ret = GLPK.glp_intopt(lp, parm);
        int stat = GLPK.glp_mip_status(lp);

        if (ret != 0) { // ret==0 indicates of the algorithm terminated correctly
            System.out.println("something went wrong");
            // GlobalAssert.that(false);
        }
        if (stat == GLPK.GLP_NOFEAS) {
            System.out.println("LP has found infeasible solution");
            // GlobalAssert.that(false);
        }

        if (stat != GLPK.GLP_OPT) {
            System.out.println("LP has found suboptimal solution");
            // GlobalAssert.that(false);
        }
    }

    private void extractSolution() {
        Map<Integer, Double> solution = new HashMap<>();
        for (int i = 1; i <= (totalSpaces * totalTaxis); i++) {
            solution.put(i, GLPK.glp_mip_col_val(lp, i));
        }

        Integer j = 1;
        for (T taxiLink : taxiLinks) {
            Integer nbTaxisToShare = taxisToGo.get(taxiLink).size();
            List<RoboTaxi> taxiToShare = new ArrayList<>(taxisToGo.get(taxiLink));
            for (T freeSpaceLink : freeSpacesLinks) {
                Integer toThisDirection = (int) Math.rint(solution.get(j));
                if (toThisDirection > 0) {
                    while (toThisDirection != 0) {
                        result.put(taxiToShare.remove(nbTaxisToShare - 1), freeSpaceLink);
                        nbTaxisToShare--;
                        toThisDirection--;
                    }
                }
                j++;
            }
        }
    }
}