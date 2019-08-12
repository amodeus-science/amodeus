/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

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

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/** Class is used to solve a transportation problem in which some
 * units @param<U> distributed in some locations @param<T> need
 * to be redistributed to other locations @param<T> with minimal
 * cost. The solution is obtained by solving a problem of the following
 * form:
 * 
 * INTEGER LINEAR PROGRAM
 * min sum_(i in origin locations) sum_(j in destination locations) cost_ij * x_ij
 * s.t.
 * (c1) x_ij >= 0
 * (c2) sum_(i in origin locations) x_ij <= dest. locations at j
 * (c3) sum_(j in dest. locations) x_ij = units at i
 * 
 *
 * @param <T> slots, e.g., roads or parking lots
 * @param <U> units, e.g., cars */
public class RedistributionProblemSolver<T, U> {

    private final Map<T, Set<U>> unitsToMove; // units to be transported
    private final Map<T, Long> availDest; // available destinations
    private final int totalOrigins;
    private final int totalDestins;
    private final List<T> originsList;
    private final List<T> destinationList;
    private final Map<U, T> result = new HashMap<>();
    private glp_prob lp;
    private glp_iocp parm;

    public RedistributionProblemSolver(Map<T, Set<U>> unitsToMove, Map<T, Long> availableDestinations, //
            BiFunction<T, T, Double> costFunction, boolean print, String exportLocation) {
        /** copying input arguments */
        this.unitsToMove = unitsToMove;
        this.availDest = availableDestinations;
        totalOrigins = unitsToMove.keySet().size();
        totalDestins = availableDestinations.keySet().size();
        // totalUnits = (int) unitsToMove.values().stream().mapToInt(s -> s.size()).sum();// .count();
        // totalSpaces = availableDestinations.values().stream()//
        // .mapToInt(l -> (int) ((long) l)).sum();// .size();
        System.out.println("total origins     : " + totalOrigins);
        System.out.println("total destinations: " + totalDestins);

        originsList = new ArrayList<>(unitsToMove.keySet());
        destinationList = new ArrayList<>(availableDestinations.keySet());
        System.out.println("starting to define lp");
        Long time = System.currentTimeMillis();
        // if there are not enough parking spaces, the problem is infeasible
        // an optimal solution is not defined.
        GlobalAssert.that(totalOrigins <= totalDestins);

        /** definition of LP */
        lp = defineLP(costFunction);
        Long time2 = System.currentTimeMillis();
        Long elapsed = time2 - time;
        System.out.println("time to define:            " + elapsed.toString());

        /** export */
        if (print) {
            String fileName = exportLocation + "/redistributionProblem.lp";
            GLPK.glp_write_lp(lp, null, fileName);
            // GLPK.glp_write_ipt(lp, fileName);
        }

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

    public Map<U, T> returnSolution() {
        return result;
    }

    private glp_prob defineLP(BiFunction<T, T, Double> costFunction) {
        try {
            lp = GLPK.glp_create_prob();

            /** problem definition */
            GLPK.glp_add_cols(lp, totalOrigins * totalDestins);

            /** creating a map of variables */
            int index = 1;
            Map<T, Map<T, Integer>> indexMap = new HashMap<>();
            for (T origin : originsList) {
                indexMap.put(origin, new HashMap<>());
                for (T destination : destinationList) {
                    indexMap.get(origin).put(destination, index);
                    ++index;
                }
            }

            /** optimization variables and cost */
            // Integer j = 1;
            int k1 = 0;
            for (T origin : originsList) {
                ++k1;
                int k2 = 0;
                for (T destination : destinationList) {
                    ++k2;
                    int varIndex = indexMap.get(origin).get(destination);
                    GLPK.glp_set_col_kind(lp, varIndex, GLPKConstants.GLP_IV);
                    GLPK.glp_set_col_bnds(lp, varIndex, GLPKConstants.GLP_LO, 0, 0);
                    GLPK.glp_set_obj_coef(lp, varIndex, costFunction.apply(origin, destination));
                    GLPK.glp_set_col_name(lp, varIndex, "x_" + k1 + "_" + k2);
                    // j++;
                }
            }

            /** create equality constraints */
            int j = 1;
            for (T origin : originsList) {
                GLPK.glp_add_rows(lp, 1);
                GLPK.glp_set_row_bnds(lp, j, GLPKConstants.GLP_FX, unitsToMove.get(origin).size(), //
                        unitsToMove.get(origin).size());
                SWIGTYPE_p_int ind = GLPK.new_intArray((totalDestins * totalOrigins) + 1);
                SWIGTYPE_p_double val = GLPK.new_doubleArray((totalDestins * totalOrigins) + 1);
                for (int k = 1; k <= (totalDestins * totalOrigins); k++) {
                    GLPK.intArray_setitem(ind, k, k);
                    if ((k <= (j * totalDestins)) & (k > ((j - 1) * totalDestins))) {
                        GLPK.doubleArray_setitem(val, k, 1);
                    } else {
                        GLPK.doubleArray_setitem(val, k, 0);
                    }
                }
                GLPK.glp_set_mat_row(lp, j, totalDestins * totalOrigins, ind, val);
                GLPK.delete_intArray(ind);
                GLPK.delete_doubleArray(val);
                j++;
            }
            GlobalAssert.that(j != totalOrigins);

            /** create inequality constraint */
            for (T destination : destinationList) {
                System.err.println("dest: " + destination.toString());
                System.err.println(availDest.get(destination));
                GLPK.glp_add_rows(lp, 1);
                GLPK.glp_set_row_bnds(lp, j, //
                        GLPKConstants.GLP_UP, availDest.get(destination), availDest.get(destination));
                SWIGTYPE_p_int ind = GLPK.new_intArray((totalDestins * totalOrigins) + 1);
                SWIGTYPE_p_double val = GLPK.new_doubleArray((totalDestins * totalOrigins) + 1);
                for (int k = 1; k <= (totalDestins * totalOrigins); k++) {
                    GLPK.intArray_setitem(ind, k, k);
                    if (Math.floorMod(k - 1, totalDestins) == 0) {
                        GLPK.doubleArray_setitem(val, k, 1);
                    } else {
                        GLPK.doubleArray_setitem(val, k, 0);
                    }
                }
                GLPK.glp_set_mat_row(lp, j, totalDestins * totalOrigins, ind, val);
                GLPK.delete_intArray(ind);
                GLPK.delete_doubleArray(val);
                j++;
            }

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
        for (int i = 1; i <= (totalDestins * totalOrigins); i++) {
            solution.put(i, GLPK.glp_mip_col_val(lp, i));
        }

        Integer j = 1;
        for (T origin : originsList) {
            Integer numberOfUnits = unitsToMove.get(origin).size();
            List<U> units = new ArrayList<>(unitsToMove.get(origin));
            for (T destination : destinationList) {
                Integer toThisDirection = (int) Math.rint(solution.get(j));
                if (toThisDirection > 0) {
                    while (toThisDirection != 0) {
                        result.put(units.remove(numberOfUnits - 1), destination);
                        numberOfUnits--;
                        toThisDirection--;
                    }
                }
                j++;
            }
        }
    }
}