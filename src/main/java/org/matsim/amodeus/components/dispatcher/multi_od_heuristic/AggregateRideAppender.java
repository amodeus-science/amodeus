package org.matsim.amodeus.components.dispatcher.multi_od_heuristic;

import org.matsim.amodeus.components.dispatcher.multi_od_heuristic.aggregation.AggregatedRequest;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;

public interface AggregateRideAppender {
    void schedule(AggregatedRequest request, DvrpVehicle vehicle, double now);

    void update();
}
