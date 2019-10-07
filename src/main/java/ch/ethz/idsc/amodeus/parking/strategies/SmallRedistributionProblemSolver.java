/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

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
        unitsToMove.values().forEach(i -> {
            if (i > 1) {
                globOptimSolPossible = false;
            }
        });

        int unitsTotal = unitsToMove.values().stream().mapToInt(i -> i).sum();

        /** this is only entered if every origin is unique */
        if (!globOptimSolPossible) {
            foundGlobOptimSol = false;
            return;
        }

        // the full LP based solver must be used if more than 1 unit
        // at some origin...
        List<T> unitsMultiplicity = new ArrayList<>();
        unitsToMove.entrySet().forEach(e -> {
            int units = e.getValue();
            GlobalAssert.that(units == 0 || units == 1);
            if (units == 1) {
                unitsMultiplicity.add(e.getKey());
            }
        });

        // find best destination for every unit
        Map<T, Integer> bestDestinationCount = new HashMap<>();
        Map<T, T> bestPairs = new HashMap<>();
        for (T origin : unitsMultiplicity) {
            NavigableMap<Double, T> lowestCost = new TreeMap<>();
            for (T destin : availableDestinations.keySet()) {
                Double cost = costFunction.apply(origin, destin);
                lowestCost.put(cost, destin);
            }
            T bestDestin = lowestCost.firstEntry().getValue();
            if (!bestDestinationCount.containsKey(bestDestin))
                bestDestinationCount.put(bestDestin, 0);
            bestDestinationCount.put(bestDestin, bestDestinationCount.get(bestDestin) + 1);
            bestPairs.put(origin, bestDestin);

        }

        // assess validity: every no conflicting best destinations
        for (Entry<T, Integer> bestDestEntry : bestDestinationCount.entrySet()) {
            if (bestDestEntry.getValue() > availableDestinations.get(bestDestEntry.getKey())) {
                globOptimSolPossible = false;
                break;
            }
        }

        // at this point, a valid solution has been found
        if (globOptimSolPossible) {
            for (T origin : unitsMultiplicity) {
                solution.put(origin, new HashMap<>());
                solution.get(origin).put(bestPairs.get(origin), 1);
            }
            foundGlobOptimSol = true;
        }

        // DEBUGGING, TODO remove
        // if (unitsTotal == 1) {
        // if (!foundGlobOptimSol) {
        // System.out.println("unitsToMove: ");
        // unitsToMove.entrySet().forEach(e -> {
        // System.out.println(getName.apply(e.getKey()) + ", " + e.getValue());
        // });
        //
        // System.out.println("availableDestinations");
        // availableDestinations.entrySet().forEach(e -> {
        // System.out.println(getName.apply(e.getKey()) + ", " + e.getValue());
        // });
        // }
        // }
    }

    public boolean success() {
        return foundGlobOptimSol;
    }

    public Map<T, Map<T, Integer>> returnSolution() {
        return solution;
    }

}