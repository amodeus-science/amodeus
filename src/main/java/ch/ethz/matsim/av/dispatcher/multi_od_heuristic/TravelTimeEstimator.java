package ch.ethz.matsim.av.dispatcher.multi_od_heuristic;

import org.matsim.api.core.v01.network.Link;

public interface TravelTimeEstimator {
    double estimateTravelTime(Link fromLink, Link toLink, double startTime);
    double getTravelTimeThreshold();
}
