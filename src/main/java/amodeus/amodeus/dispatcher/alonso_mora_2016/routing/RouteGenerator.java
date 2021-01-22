package amodeus.amodeus.dispatcher.alonso_mora_2016.routing;

import amodeus.amodeus.dispatcher.alonso_mora_2016.routing.DefaultTravelFunction.PartialSolution;

public interface RouteGenerator {
    PartialSolution next();

    void expand(PartialSolution solution, double updatedTime, int updatedPassengers, double updatedCost);

    boolean hasNext();
}