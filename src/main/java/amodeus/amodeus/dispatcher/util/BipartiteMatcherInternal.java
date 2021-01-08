/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Map;

import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.core.router.FastAStarLandmarksFactory;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.SharedUniversalDispatcher;
import amodeus.amodeus.routing.CachedNetworkTimeDistance;
import amodeus.amodeus.routing.DistanceFunction;
import amodeus.amodeus.routing.NetworkMinTimeDistanceFunction;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/* package */ abstract class BipartiteMatcherInternal implements BipartiteMatcher {

    /** network distance function used to prevent cycling solutions */
    protected final DistanceFunction accDstFctn;

    public BipartiteMatcherInternal(Network network) {
        accDstFctn = new NetworkMinTimeDistanceFunction(network, new FastAStarLandmarksFactory(Runtime.getRuntime().availableProcessors()));
    }

    @Override
    public final Tensor executePickup(SharedUniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<PassengerRequest> requests, /** <- typically universalDispatcher.getPassengerRequests() */
            DistanceFunction distanceFunction, Network network) {
        return executeGeneralPickup(universalDispatcher, roboTaxis, requests, distanceFunction, null, network);
    }

    protected final Tensor executeGeneralPickup(SharedUniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<PassengerRequest> requests, /** <- typically universalDispatcher.getPassengerRequests() */
            DistanceFunction distanceFunction, CachedNetworkTimeDistance distanceCashed, Network network) {
        Tensor infoLine = Tensors.empty();
        Map<RoboTaxi, PassengerRequest> gbpMatchCleaned = getGBPMatch(universalDispatcher, roboTaxis, requests, distanceFunction, network);
        /** perform dispatching */
        gbpMatchCleaned.forEach((rt, req) -> universalDispatcher.setRoboTaxiPickup(rt, req, Double.NaN, Double.NaN));
        return infoLine;
    }

    public abstract Map<RoboTaxi, PassengerRequest> getGBPMatch(SharedUniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<PassengerRequest> requests, /** <- typically universalDispatcher.getPassengerRequests() */
            DistanceFunction distanceFunction, Network network);
}
