package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.FastAStarLandmarksFactory;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.UniversalDispatcher;
import ch.ethz.idsc.amodeus.routing.CachedNetworkTimeDistance;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.amodeus.routing.NetworkMinTimeDistanceFunction;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.matsim.av.passenger.AVRequest;

public abstract class BipartiteMatchingUtilsSuper implements BipartiteMatchingUtilsInterface {

    /** network distance function used to prevent cycling solutions */
    protected final DistanceFunction accDstFctn;

    public BipartiteMatchingUtilsSuper(Network network) {
        accDstFctn = new NetworkMinTimeDistanceFunction(network, new FastAStarLandmarksFactory());
    }

    @Override
    public Tensor executePickup(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            DistanceFunction distanceFunction, Network network) {
        // time irrelevant for this call
        return executeGeneralPickup(universalDispatcher, roboTaxis, requests, distanceFunction, null, network);// , -10.0, false);

    }

    protected abstract Tensor executeGeneralPickup(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            DistanceFunction distanceFunction, CachedNetworkTimeDistance distanceCashed, Network network);// , double time, boolean cached);
}
