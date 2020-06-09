/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.concurrent.Future;

import org.matsim.amodeus.plpc.ParallelLeastCostPathCalculator;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelTime;

/** InstantPathFactory may be used by 3rd party dispatchers to compute routes
 * Example: MPCDispatcher */
public class InstantPathFactory {
    private final ParallelLeastCostPathCalculator parallelLeastCostPathCalculator;
    private final TravelTime travelTime;

    public InstantPathFactory( //
            ParallelLeastCostPathCalculator parallelLeastCostPathCalculator, //
            TravelTime travelTime) {
        this.parallelLeastCostPathCalculator = parallelLeastCostPathCalculator;
        this.travelTime = travelTime;
    }

    public VrpPathWithTravelData getVrpPathWithTravelData(Link startLink, Link destLink, double startTime) {
        Future<Path> leastCostPathFuture = parallelLeastCostPathCalculator.calcLeastCostPath( // <- non-blocking call
                startLink.getToNode(), destLink.getFromNode(), startTime, null, null);
        return new FuturePathContainer(startLink, destLink, startTime, leastCostPathFuture, travelTime).getVrpPathWithTravelData();
    }
}
