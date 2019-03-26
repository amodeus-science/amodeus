/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
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

public class BipartiteMatchingUtils {

    /** network distance function used to prevent cycling solutions */
    private final DistanceFunction accDstFctn;

    public BipartiteMatchingUtils(Network network) {
        accDstFctn = new NetworkMinTimeDistanceFunction(network, new FastAStarLandmarksFactory());
    }

    public Tensor executePickup(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            DistanceFunction distanceFunction, Network network) {
        // time irrelevant for this call
        return executeGeneralPickup(universalDispatcher, roboTaxis, requests, distanceFunction, null, network, -10.0, false);

    }

    public Tensor executeCached(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            CachedNetworkTimeDistance distanceCashed, Network network, double now) {
        // time irrelevant for this call
        return executeGeneralPickup(universalDispatcher, roboTaxis, requests, null, distanceCashed, network, now, true);
    }

    private Tensor executeGeneralPickup(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            DistanceFunction distanceFunction, CachedNetworkTimeDistance distanceCashed, Network network, double time, boolean cached) {
        Tensor infoLine = Tensors.empty();

        Map<RoboTaxi, AVRequest> gbpMatchCleaned = cached ? //
                getGBPMatchCached(universalDispatcher, roboTaxis, requests, distanceCashed, network, time) : //
                getGBPMatch(universalDispatcher, roboTaxis, requests, distanceFunction, network);

        /** perform dispatching */
        for (Entry<RoboTaxi, AVRequest> entry : gbpMatchCleaned.entrySet())
            universalDispatcher.setRoboTaxiPickup(entry.getKey(), entry.getValue());

        return infoLine;
    }

    public Map<RoboTaxi, AVRequest> getGBPMatch(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            DistanceFunction distanceFunction, Network network) {

        /** reduction of problem size with kd-tree, helps to downsize problems where n << m or m>> n
         * for n number of available taxis and m number of available requests */
        Map<RoboTaxi, AVRequest> gbpMatch = ((new GlobalBipartiteMatching(distanceFunction)).match(roboTaxis, requests));

        /** prevent cycling an assignment is only updated if the new distance is smaller than the
         * old distance */
        Map<RoboTaxi, AVRequest> gbpMatchCleaned = CyclicSolutionPreventer.apply(gbpMatch, universalDispatcher, accDstFctn);

        return gbpMatchCleaned;
    }

    public Map<RoboTaxi, AVRequest> getGBPMatchCached(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            CachedNetworkTimeDistance distanceCashed, Network network, double now) {

        /** reduction of problem size with kd-tree, helps to downsize problems where n << m or m>> n
         * for n number of available taxis and m number of available requests */
        Map<RoboTaxi, AVRequest> gbpMatch = ((new GlobalBipartiteMatchingCached(distanceCashed)).match(roboTaxis, requests, now));

        return gbpMatch;
    }

}
