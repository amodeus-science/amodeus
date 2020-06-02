/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.parking.strategies;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import amodeus.amodeus.util.math.GlobalAssert;

/** Class is used to solve a transportation problem in which some
 * units distributed in some locations @param<T> need
 * to be redistributed to other locations @param<T> with minimal
 * cost.The solution is normally obtained by solving a problem of the following
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
 * In this class which is only called for very small numbers of units to be
 * distributed, a solution via enumeration is attempted to be found to increase
 * process speed if solutions of {1,2,3,...} units have to be found with a high
 * sampling rate. */
public class SmallRedistributionProblemSolver<T> {
    private boolean globOptimSolPossible = true;
    private boolean foundGlobOptimSol = false;
    protected final Map<T, Map<T, Integer>> solution = new HashMap<>();

    public SmallRedistributionProblemSolver(Map<T, Integer> unitsToMove, //
            Map<T, Integer> availableDestinations, //
            BiFunction<T, T, Double> costFunction, //
            Function<T, String> getName, boolean print, String exportLocation) {

        /** checking if at most unit to move per origin */
        globOptimSolPossible = unitsToMove.values().stream().noneMatch(i -> i > 1);

        // int unitsTotal = unitsToMove.values().stream().mapToInt(i -> i).sum();

        /** this is only entered if every origin is unique */
        if (!globOptimSolPossible) {
            foundGlobOptimSol = false;
            return;
        }

        // the full LP based solver must be used if more than 1 unit
        // at some origin...
        List<T> unitsMultiplicity = new ArrayList<>();
        unitsToMove.forEach((t, units) -> {
            GlobalAssert.that(units == 0 || units == 1);
            if (units == 1)
                unitsMultiplicity.add(t);
        });

        // find best destination for every unit
        Map<T, Integer> bestDestinationCount = new HashMap<>();
        Map<T, T> bestPairs = new HashMap<>();
        for (T origin : unitsMultiplicity) {
            T bestDestin = availableDestinations.keySet().stream().min(Comparator.comparingDouble(dest -> costFunction.apply(origin, dest))).get();
            bestDestinationCount.merge(bestDestin, 1, Integer::sum);
            bestPairs.put(origin, bestDestin);
        }

        // assess validity: every no conflicting best destinations
        globOptimSolPossible = bestDestinationCount.entrySet().stream().noneMatch(e -> e.getValue() > availableDestinations.get(e.getKey()));

        // at this point, a valid solution has been found
        if (globOptimSolPossible) {
            for (T origin : unitsMultiplicity)
                solution.computeIfAbsent(origin, o -> new HashMap<>()) //
                        /* solution.get(origin) */ .put(bestPairs.get(origin), 1);
            foundGlobOptimSol = true;
        }

    }

    public boolean success() {
        return foundGlobOptimSol;
    }

    public Map<T, Map<T, Integer>> returnSolution() {
        return solution;
    }
}