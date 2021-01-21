package amodeus.amodeus.dispatcher.alonso_mora_2016.sequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import amodeus.amodeus.dispatcher.alonso_mora_2016.sequence.ExtensiveTravelFunction.PartialSolution;

public class PartialRouteGenerator {
    private final int numberOfDirectives;
    private final boolean isDepthFirst;

    private final List<PartialSolution> queue = new LinkedList<>();

    public PartialRouteGenerator(double now, int numberOfDirectives, boolean isDepthFirst) {
        this.numberOfDirectives = numberOfDirectives;
        this.isDepthFirst = isDepthFirst;

        for (int i = 0; i < numberOfDirectives; i++) {
            queue.add(new PartialSolution(Arrays.asList(), i, now, 0.0));
        }
    }

    public PartialSolution next() {
        return queue.remove(isDepthFirst ? queue.size() - 1 : 0);
    }

    public void expand(PartialSolution solution, double updatedTime, int updatedPassengers, double updatedCost) {
        List<Integer> updatedIndices = new ArrayList<>(solution.indices.size() + 1);
        updatedIndices.addAll(solution.indices);
        updatedIndices.add(solution.addedIndex);

        for (int i = 0; i < numberOfDirectives; i++) {
            if (!solution.indices.contains(i)) {

                queue.add(new PartialSolution(updatedIndices, i, updatedTime, updatedCost));
            }
        }
    }

    public boolean hasNext() {
        return queue.size() > 0;
    }
}
