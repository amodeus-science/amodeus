package ch.ethz.matsim.av.dispatcher.multi_od_heuristic;

import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.multi_od_heuristic.aggregation.AggregatedRequest;

public interface AggregateRideAppender {
    void schedule(AggregatedRequest request, AVVehicle vehicle, double now);
    void update();
}
