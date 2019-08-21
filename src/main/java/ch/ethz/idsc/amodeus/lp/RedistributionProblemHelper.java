/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public enum RedistributionProblemHelper {
    ;

    /** @return a {@link Map} containing the number of units @param <U>
     *         that must be moved to a set of locations @param <T> according
     *         to the parameter @param unitsToMove, sample usage:
     * 
     *         Map<Link, Set<RoboTaxi>> taxisToGo;
     *         Map<Link, Integer> unitsToMove = RedistributionProblemHelper.getFlow(taxisToGo); */
    public static <T, U> Map<T, Integer> getFlow(Map<T, Set<U>> unitsToMove) {
        Map<T, Integer> quantitiesToMove = new HashMap<>();
        unitsToMove.entrySet().forEach(e -> {
            quantitiesToMove.put(e.getKey(), e.getValue().size());
        });
        return quantitiesToMove;
    }

    /** @return a {@link Map} with command pairs @param <U> @param <T> that should be
     *         executed in order to have a valid execution of the @param flowSolution to be
     *         executed on the units @param unitsToMove, sample usage:
     * 
     *         Map<Link, Set<RoboTaxi>> taxisToGo
     *         Map<Link, Integer> freeSpaces
     * 
     *         Map<Link, Map<Link, Integer>> flowSolution = flowLP.returnSolution();
     *         return RedistributionProblemHelper.getSolutionCommands(taxisToGo, flowSolution);
     * 
     * 
     *         where flowLP is an instance of the {@link RedistributionProblemSolverMILP} */
    public static <T, U> Map<U, T> getSolutionCommands(Map<T, Set<U>> unitsToMove, //
            Map<T, Map<T, Integer>> flowSolution) {
        Map<U, T> sendCommandMap = new HashMap<>();
        for (T origin : flowSolution.keySet()) {
            List<U> toSend = new ArrayList<>();
            unitsToMove.get(origin).stream().forEach(t -> toSend.add(t));
            Map<T, Integer> destinationMap = flowSolution.get(origin);
            /** produce a list of all destinations with multiple entries, e.g.,
             * 1 vehicle to dest 1, 2 vehicles to dest 2--> {1,2,2} */
            List<T> destinations = new ArrayList<>();
            for (T dest : destinationMap.keySet()) {
                for (int i = 0; i < destinationMap.get(dest); ++i) {
                    destinations.add(dest);
                }
            }
            /** it can be that toSend has larger dimension than destinations, not
             * the other way around */
            GlobalAssert.that(destinations.size() <= toSend.size());
            for (int i = 0; i < destinations.size(); ++i) {
                sendCommandMap.put(toSend.get(i), destinations.get(i));
            }
        }
        return sendCommandMap;
    }

}
