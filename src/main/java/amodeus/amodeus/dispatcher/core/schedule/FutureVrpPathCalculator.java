package amodeus.amodeus.dispatcher.core.schedule;

import java.util.concurrent.Future;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.matsim.amodeus.plpc.ParallelLeastCostPathCalculator;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelTime;

public class FutureVrpPathCalculator {
    private final ParallelLeastCostPathCalculator pathCalculator;
    private final TravelTime travelTime;

    static private final double FIRST_LINK_TT = 1.0;
    static private final double ESTIMATED_DURATION = 600.0;

    public FutureVrpPathCalculator(ParallelLeastCostPathCalculator pathCalculator, TravelTime travelTime) {
        this.pathCalculator = pathCalculator;
        this.travelTime = travelTime;
    }

    /** Based on VrpPaths.calcAndCreatePath */
    public VrpPathWithTravelData calculatePath(Link fromLink, Link toLink, double departureTime) {
        Future<Path> path = ConcurrentUtils.constantFuture(null);

        if (fromLink != toLink) {
            path = pathCalculator.calcLeastCostPath(fromLink.getToNode(), toLink.getFromNode(), departureTime + FIRST_LINK_TT, null, null);
        }

        double estimatedArrivalTime = departureTime + ESTIMATED_DURATION;
        return new FutureVrpPathWithTravelData(departureTime, estimatedArrivalTime, fromLink, toLink, path, travelTime);
    }
}
