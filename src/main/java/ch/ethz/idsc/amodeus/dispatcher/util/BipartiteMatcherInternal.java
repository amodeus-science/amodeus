/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Map;

import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.core.router.FastAStarLandmarksFactory;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.UniversalDispatcher;
import ch.ethz.idsc.amodeus.routing.CachedNetworkTimeDistance;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.amodeus.routing.NetworkMinTimeDistanceFunction;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/* package */ abstract class BipartiteMatcherInternal implements BipartiteMatcher {

    /** network distance function used to prevent cycling solutions */
    protected final DistanceFunction accDstFctn;

    public BipartiteMatcherInternal(Network network) {
        accDstFctn = new NetworkMinTimeDistanceFunction(network, new FastAStarLandmarksFactory(Runtime.getRuntime().availableProcessors()));
    }

    @Override
    public final Tensor executePickup(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<PassengerRequest> requests, /** <- typically universalDispatcher.getPassengerRequests() */
            DistanceFunction distanceFunction, Network network) {
        return executeGeneralPickup(universalDispatcher, roboTaxis, requests, distanceFunction, null, network);
    }

    protected final Tensor executeGeneralPickup(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<PassengerRequest> requests, /** <- typically universalDispatcher.getPassengerRequests() */
            DistanceFunction distanceFunction, CachedNetworkTimeDistance distanceCashed, Network network) {
        Tensor infoLine = Tensors.empty();
        Map<RoboTaxi, PassengerRequest> gbpMatchCleaned = getGBPMatch(universalDispatcher, roboTaxis, requests, distanceFunction, network);
        /** perform dispatching */
        gbpMatchCleaned.forEach(universalDispatcher::setRoboTaxiPickup);
        return infoLine;
    }

    public abstract Map<RoboTaxi, PassengerRequest> getGBPMatch(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<PassengerRequest> requests, /** <- typically universalDispatcher.getPassengerRequests() */
            DistanceFunction distanceFunction, Network network);
}
