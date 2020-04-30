package ch.ethz.matsim.av.dispatcher.multi_od_heuristic;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.geometry.CoordUtils;

public class FactorTravelTimeEstimator implements TravelTimeEstimator {
    private final double threshold;

    public FactorTravelTimeEstimator(double threshold) {
        this.threshold = threshold;
    }

    public double estimateTravelTime(Link fromLink, Link toLink, double startTime) {
        return ((CoordUtils.calcEuclideanDistance(fromLink.getCoord(), toLink.getCoord()) / 1000.0) / 30.0) * 3600.0;
    }

    public double getTravelTimeThreshold() {
        return threshold;
    }
}
