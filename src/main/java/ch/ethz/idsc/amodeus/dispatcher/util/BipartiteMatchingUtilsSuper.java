package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.FastAStarLandmarksFactory;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.UniversalDispatcher;
import ch.ethz.idsc.amodeus.routing.CachedNetworkTimeDistance;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.amodeus.routing.NetworkMinTimeDistanceFunction;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ abstract class BipartiteMatchingUtilsSuper implements BipartiteMatcher {

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
        return executeGeneralPickup(universalDispatcher, roboTaxis, requests, distanceFunction, null, network);
    }

    protected Tensor executeGeneralPickup(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            DistanceFunction distanceFunction, CachedNetworkTimeDistance distanceCashed, Network network) {
        Tensor infoLine = Tensors.empty();
        Map<RoboTaxi, AVRequest> gbpMatchCleaned = getGBPMatch(universalDispatcher, roboTaxis, requests, distanceFunction, network);
        /** perform dispatching */
        for (Entry<RoboTaxi, AVRequest> entry : gbpMatchCleaned.entrySet())
            universalDispatcher.setRoboTaxiPickup(entry.getKey(), entry.getValue());
        return infoLine;
    }

    public abstract Map<RoboTaxi, AVRequest> getGBPMatch(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            DistanceFunction distanceFunction, Network network);

}
