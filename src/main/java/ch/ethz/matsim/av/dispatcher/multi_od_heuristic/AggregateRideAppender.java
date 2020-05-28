package ch.ethz.matsim.av.dispatcher.multi_od_heuristic;

import org.matsim.contrib.dvrp.fleet.DvrpVehicle;

import ch.ethz.matsim.av.dispatcher.multi_od_heuristic.aggregation.AggregatedRequest;

public interface AggregateRideAppender {
    void schedule(AggregatedRequest request, DvrpVehicle vehicle, double now);

    void update();
}
