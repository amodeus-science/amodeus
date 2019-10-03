/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
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

    private Boolean success = null;
    protected final Map<T, Map<T, Integer>> solution = new HashMap<>();

    public SmallRedistributionProblemSolver(Map<T, Integer> unitsToMove, //
            Map<T, Integer> availableDestinations, //
            BiFunction<T, T, Double> costFunction, //
            Function<T, String> getName, boolean print, String exportLocation) {

        unitsToMove.values().forEach(i -> {
            if (i > 1) {
                success = false;
            }
        });

        /** this is only entered if every origin is unique */
        if (Objects.isNull(success)) {
            List<T> unitsMultiplicity = new ArrayList<>();
            unitsToMove.keySet().forEach(t -> unitsMultiplicity.add(t));

            GlobalAssert.that(unitsMultiplicity.size() == unitsToMove.size());

            Map<T, Integer> bestDestins = new HashMap<>();
            Map<T, T> bestPairs = new HashMap<>();
            for (T origin : unitsMultiplicity) {
                NavigableMap<Double, T> lowestCost = new TreeMap<>();
                for (T destin : availableDestinations.keySet()) {
                    Double cost = costFunction.apply(origin, destin);
                    lowestCost.put(cost, destin);
                }
                T bestDestin = lowestCost.firstEntry().getValue();
                if (!bestDestins.containsKey(bestDestin))
                    bestDestins.put(bestDestin, 0);
                bestDestins.put(bestDestin, bestDestins.get(bestDestin) + 1);
                bestPairs.put(origin, bestDestin);

                // assess validity
                for (Entry<T, Integer> entry : bestDestins.entrySet()) {
                    if (entry.getValue() > availableDestinations.get(entry.getKey())) {
                        success = false;
                        break;
                    }
                }

            }

            // at this point, a valid solution has been found
            if (Objects.isNull(success)) {
                for (T origin : unitsMultiplicity) {
                    solution.put(origin, new HashMap<>());
                    solution.get(origin).put(bestPairs.get(origin), 1);
                }
                success = true;
            }
        }

    }

    public boolean success() {
        return success;
    }

    public Map<T, Map<T, Integer>> returnSolution() {
        return solution;
    }

}