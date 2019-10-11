/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.GlpkException;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.glp_smcp;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/** Class is used to solve a transportation problem in which some
 * units distributed in some locations @param<T> need
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
 * (c4) x_ij in {0,1,2,...}
 * 
 *
 * @param <T> slots, e.g., roads or parking lots. The problem has a
 *            totally unimodular costraint matrix and can thus be solved without
 *            integrality constraints (c4) */
public class RedistributionProblemSolver<T> {

    private final Function<T, String> getName;
    private final Map<T, Integer> unitsToMove; // units to be transported
    private final Map<T, Integer> availDest; // available destinations
    protected final int totalOrigins;
    protected final int totalDestins;
    protected final List<T> originsList;
    protected final List<T> destinationList;
    protected final Map<T, Map<T, Integer>> indexMap = new HashMap<>();
    protected final Map<T, Map<T, Integer>> solution = new HashMap<>();
    private final Map<T, Map<T, Double>> dblSolut = new HashMap<>();
    protected glp_prob lp;

    public RedistributionProblemSolver(Map<T, Integer> unitsToMove, Map<T, Integer> availableDestinations, //
            BiFunction<T, T, Double> costFunction, Function<T, String> getName, boolean print, String exportLocation) {
        /** copying input arguments */
        this.getName = getName;
        this.unitsToMove = unitsToMove;
        this.availDest = availableDestinations;
        totalOrigins = unitsToMove.keySet().size();
        totalDestins = availableDestinations.keySet().size();
        System.out.println("total origins     : " + totalOrigins);
        System.out.println("total destinations: " + totalDestins);

        originsList = new ArrayList<>(unitsToMove.keySet());
        destinationList = new ArrayList<>(availableDestinations.keySet());
        System.out.println("starting to define lp");
        Long time = System.currentTimeMillis();

        int totalUnits = unitsToMove.values().stream().mapToInt(i -> i).sum();
        int totalSpots = availDest.values().stream().mapToInt(i -> i).sum();
        System.out.println("total units to move:   " + totalUnits);
        System.out.println("total spots available: " + totalSpots);
        // if there are not enough parking spaces, the problem is infeasible
        // an optimal solution is not defined.
        GlobalAssert.that(totalUnits <= totalSpots);

        /** definition of LP */
        lp = defineLP(costFunction);
        Long time2 = System.currentTimeMillis();
        Long elapsed = time2 - time;
        System.out.println("time to define:            " + elapsed.toString());

        /** export */
        if (print) {
            String fileName = exportLocation + "/redistributionProblemLP.lp";
            GLPK.glp_write_lp(lp, null, fileName);
        }

        /** solving LP */
        solveLP(print);
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

    public Map<T, Map<T, Integer>> returnSolution() {
        return solution;
    }

    public Map<T, Map<T, Double>> returnDoubleSolution() {
        return dblSolut;
    }

    private glp_prob defineLP(BiFunction<T, T, Double> costFunction) {
        try {
            lp = GLPK.glp_create_prob();
            GLPK.glp_set_obj_name(lp, this.getClass().getSimpleName());

            /** problem definition */
            GLPK.glp_add_cols(lp, totalOrigins * totalDestins);
            // x_ij with i in {1,...,totalOrigins}, j in {1,...,totalDestinations}
            int numVar = totalDestins * totalOrigins;

            /** creating a map of variables */
            int index = 1;
            for (T origin : originsList) {
                indexMap.put(origin, new HashMap<>());
                for (T destination : destinationList) {
                    indexMap.get(origin).put(destination, index);
                    ++index;
                }
            }

            /** optimization variables and cost */
            for (T origin : originsList) {
                for (T destination : destinationList) {
                    int varIndex = indexMap.get(origin).get(destination);
                    GLPK.glp_set_col_kind(lp, varIndex, GLPKConstants.GLP_IV);
                    GLPK.glp_set_col_bnds(lp, varIndex, GLPKConstants.GLP_LO, 0, 0);
                    GLPK.glp_set_obj_coef(lp, varIndex, costFunction.apply(origin, destination));
                    GLPK.glp_set_col_name(lp, varIndex, "f_" + getName.apply(origin) + "_" + destination.toString());
                }
            }

            /** create equality constraints */
            int constrIndex = 1;
            for (T origin : originsList) {
                Integer toMove = unitsToMove.get(origin);
                GLPK.glp_add_rows(lp, 1);
                GLPK.glp_set_row_bnds(lp, constrIndex, GLPKConstants.GLP_FX, toMove, -1);
                SWIGTYPE_p_int ind = GLPK.new_intArray(numVar + 1);
                SWIGTYPE_p_double val = GLPK.new_doubleArray(numVar + 1);

                /** initialize A matrix with 0 for all elements */
                for (int k = 1; k <= numVar; k++) {
                    GLPK.intArray_setitem(ind, k, k);
                    GLPK.doubleArray_setitem(val, k, 0);
                }

                /** set 1 for all possible destination nodes, i.e,
                 * create constraint sum_j x_ij == 1 */
                for (T destin : destinationList) {
                    int k = indexMap.get(origin).get(destin);
                    GLPK.doubleArray_setitem(val, k, 1);
                }

                GLPK.glp_set_mat_row(lp, constrIndex, numVar, ind, val);
                GLPK.delete_intArray(ind);
                GLPK.delete_doubleArray(val);
                constrIndex++;
            }
            GlobalAssert.that(constrIndex != totalOrigins);

            /** create inequality constraint */
            for (T destination : destinationList) {
                GLPK.glp_add_rows(lp, 1);
                long availableSpots = availDest.get(destination);
                GLPK.glp_set_row_bnds(lp, constrIndex, GLPKConstants.GLP_UP, -1, availableSpots);
                SWIGTYPE_p_int ind = GLPK.new_intArray(numVar + 1);
                SWIGTYPE_p_double val = GLPK.new_doubleArray(numVar + 1);

                /** initialize A matrix with 0 for all elements */
                for (int k = 1; k <= numVar; k++) {
                    GLPK.intArray_setitem(ind, k, k);
                    GLPK.doubleArray_setitem(val, k, 0);
                }

                /** set 1 for all possible destination nodes, i.e,
                 * create constraint sum_i x_ij <= availableSpots */
                for (T origin : originsList) {
                    int k = indexMap.get(origin).get(destination);
                    GLPK.doubleArray_setitem(val, k, 1);
                }

                GLPK.glp_set_mat_row(lp, constrIndex, numVar, ind, val);
                GLPK.delete_intArray(ind);
                GLPK.delete_doubleArray(val);
                constrIndex++;
            }

        } catch (GlpkException e) {
            e.printStackTrace();
        }
        return lp;
    }

    protected void solveLP(boolean print) {
        glp_smcp parm = new glp_smcp();
        GLPK.glp_init_smcp(parm);
        parm.setPresolve(GLPK.GLP_ON);
        int ret = GLPK.glp_simplex(lp, parm);
        int stat = GLPK.glp_get_status(lp);

        if (print)
            printSolution();

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

    protected void printSolution() {
        for (int i = 1; i <= (totalDestins * totalOrigins); i++) {
            System.err.println("varindex: " + i);
            String name = GLPK.glp_get_col_name(lp, i);
            System.err.println(name + "\t=\t" + GLPK.glp_get_col_prim(lp, i));
            System.err.println("+++");
        }
    }

    protected void extractSolution() {
        for (T origin : originsList) {
            solution.put(origin, new HashMap<>());
            dblSolut.put(origin, new HashMap<>());
            for (T dest : destinationList) {
                int varIndex = indexMap.get(origin).get(dest);
                double result = GLPK.glp_get_col_prim(lp, varIndex);
                solution.get(origin).put(dest, (int) result);
                dblSolut.get(origin).put(dest, result);
            }
        }
    }
}