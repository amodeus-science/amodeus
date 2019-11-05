/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.glp_iocp;

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
 * 
 *
 * @param <T> slots, e.g., roads or parking lots */
@Deprecated // TODO delete later, use LP based solver. This one is exactly identical
// but it uses MI programming to solve the problem which is not necessary due to the
// totally unimodular constraint matrix.
public class RedistributionProblemSolverMILP<T> extends RedistributionProblemSolver<T> {

    public RedistributionProblemSolverMILP(Map<T, Integer> unitsToMove, Map<T, Integer> availableDestinations, //
            BiFunction<T, T, Double> costFunction, Function<T, String> getName, boolean print, String exportLocation) {
        super(unitsToMove, availableDestinations, costFunction, getName, print, exportLocation);
    }

    @Override
    public Map<T, Map<T, Integer>> returnSolution() {
        return solution;
    }

    @Override
    protected void solveLP(boolean print) {

        glp_iocp parm = new glp_iocp();
        GLPK.glp_init_iocp(parm);
        parm.setPresolve(GLPK.GLP_ON);
        int ret = GLPK.glp_intopt(lp, parm);
        int stat = GLPK.glp_mip_status(lp);

        if (print)
            printSolution();

        if (ret != 0) // ret==0 indicates of the algorithm terminated correctly
            System.out.println("something went wrong"); // throw new RuntimeException("something went wrong");
        if (stat == GLPK.GLP_NOFEAS)
            System.out.println("LP has found infeasible solution"); // throw new RuntimeException("LP has found infeasible solution");
        if (stat != GLPK.GLP_OPT)
            System.out.println("LP has found suboptimal solution"); // throw new RuntimeException("LP has found suboptimal solution");
    }

    @Override
    protected void printSolution() {
        for (int i = 1; i <= (totalDestins * totalOrigins); i++) {
            System.err.println("varindex: " + i);
            String name = GLPK.glp_get_col_name(lp, i);
            System.err.println(name + "\t=\t" + GLPK.glp_mip_col_val(lp, i));
            System.err.println("+++");
        }
    }

    @Override
    protected void extractSolution() {
        for (T origin : originsList) {
            solution.put(origin, new HashMap<>());
            for (T dest : destinationList) {
                int varIndex = indexMap.get(origin).get(dest);
                int result = (int) GLPK.glp_mip_col_val(lp, varIndex);
                solution.get(origin).put(dest, result);
            }
        }
    }
}