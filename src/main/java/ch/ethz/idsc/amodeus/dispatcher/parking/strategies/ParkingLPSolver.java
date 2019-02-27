/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking.strategies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceFunction;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

class ParkingLPSolver {

    private final Map<Link, Set<RoboTaxi>> taxisToGo;
    private final Map<Link, Long> freeSpacesToGo;

    private final DistanceFunction distanceFunction;

    private final int totalTaxis;
    private final int totalSpaces;

    private final List<Link> taxiLinks;
    private final List<Link> freeSpacesLinks;

    private final glp_prob lp;

    private final Map<Integer, Map<Link, Link>> trackingMap = new HashMap<>();

    private final Map<Integer, Double> solution;

    ParkingLPSolver(Map<Link, Set<RoboTaxi>> taxisToGo, Map<Link, Long> freeSpacesToGo, DistanceFunction distanceFunction) {
        this.taxisToGo = taxisToGo;
        this.freeSpacesToGo = freeSpacesToGo;
        this.distanceFunction = distanceFunction;

        this.totalTaxis = taxisToGo.keySet().size();
        this.totalSpaces = freeSpacesToGo.keySet().size();
        System.out.println("total taxi links: " + this.totalTaxis);
        System.out.println("total space links: " + this.totalSpaces);

        this.taxiLinks = new ArrayList<>(taxisToGo.keySet());
        this.freeSpacesLinks = new ArrayList<>(freeSpacesToGo.keySet());

        Long time = System.currentTimeMillis();
        this.lp = defineLP();
        Long time2 = System.currentTimeMillis();
        Long elapsed = time2 - time;
        System.out.println("time to define: " + elapsed.toString());
        solveLP(true);
        Long time3 = System.currentTimeMillis();
        elapsed = time3 - time2;
        System.out.println("time to solve: " + elapsed.toString());
        this.solution = writeLPSolution();
        Long time4 = System.currentTimeMillis();
        elapsed = time4 - time3;
        System.out.println("time to write: " + elapsed.toString());
    }

    private glp_prob defineLP() {
        glp_prob lp = null;
        try {
            /** INTEGER LINEAR PROGRAM
             * min sum_(i in taxiLinks) sum_(j in freeSpacesLinks) cost_ij * x_ij
             * s.t.
             * (c1) x_ij >= 0
             * (c2) sum_(i in taxiLinks) x_ij <= freeSpaces at j
             * (c3) sum_(j in freeSpacesLinks) x_ij = numberOfTaxis at i */
            lp = GLPK.glp_create_prob();

            // set up optimization variables
            GLPK.glp_add_cols(lp, totalTaxis * totalSpaces);

            // create all optimization variables with objective weight and lower bound constraint
            Integer j = 1;
            for (Link taxiLink : taxiLinks) {
                for (Link freeSpaceLink : freeSpacesLinks) {
                    GLPK.glp_set_col_kind(lp, j, GLPKConstants.GLP_IV);
                    GLPK.glp_set_col_bnds(lp, j, GLPKConstants.GLP_LO, 0, 0);
                    GLPK.glp_set_obj_coef(lp, j, getCost(taxiLink, freeSpaceLink));
                    /** TRACKING FOR LATER ASSIGNMENT */
                    Map<Link, Link> linkMap = new HashMap<>();
                    linkMap.put(taxiLink, freeSpaceLink);
                    trackingMap.put(j, linkMap);
                    j++;
                }
            }
            GlobalAssert.that(j != (totalTaxis * totalSpaces));

            // create equality constraint
            j = 1;
            for (Link taxiLink : taxiLinks) {
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
            for (Link freeSpaceLink : freeSpacesLinks) {
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
        glp_iocp parm = new glp_iocp();
        GLPK.glp_init_iocp(parm);
        parm.setPresolve(GLPK.GLP_ON);
        int ret = GLPK.glp_intopt(lp, parm);
        int stat = GLPK.glp_mip_status(lp);

        if (ret != 0) { // ret==0 indicates of the algorithm ran correctly
            System.out.println("something went wrong");
            closeLP();
            GlobalAssert.that(false);
        }
        if (stat == GLPK.GLP_NOFEAS) {
            System.out.println("LP has found infeasible solution");
            closeLP();
            GlobalAssert.that(false);
        }

        if (stat != GLPK.GLP_OPT) {
            System.out.println("LP has found suboptimal solution");
            closeLP();
            GlobalAssert.that(false);
        }
    }

    private final void closeLP() {
        GLPK.glp_delete_prob(lp);
    }

    private final Map<Integer, Double> writeLPSolution() {
        Map<Integer, Double> result = new HashMap<>();
        for (int i = 1; i <= (totalSpaces * totalTaxis); i++) {
            result.put(i, GLPK.glp_mip_col_val(lp, i));
        }

        return result;
    }

    public Map<RoboTaxi, Link> returnSolution() {
        // TODO: check if correct
        Map<RoboTaxi, Link> result = new HashMap<>();
        Integer j = 1;
        for (Link taxiLink : taxiLinks) {
            Integer nbTaxisToShare = taxisToGo.get(taxiLink).size();
            List<RoboTaxi> taxiToShare = new ArrayList<>(taxisToGo.get(taxiLink));
            for (Link freeSpaceLink : freeSpacesLinks) {
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
            GlobalAssert.that(nbTaxisToShare.equals(0));
        }

        return result;
    }

    private double getCost(Link taxiLink, Link freeSpaceLink) {
        return distanceFunction.getDistance(taxiLink, freeSpaceLink);
    }

}
