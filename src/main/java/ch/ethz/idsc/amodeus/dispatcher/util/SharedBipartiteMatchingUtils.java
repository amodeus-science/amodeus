// TODO this must be elimitated as only minor difference to BipartiteMatchingUtils
/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.FastAStarLandmarksFactory;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedUniversalDispatcher;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.amodeus.routing.NetworkMinTimeDistanceFunction;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.passenger.AVRequest;

public class SharedBipartiteMatchingUtils {

    /** network distance function used to prevent cycling solutions */
    private final DistanceFunction accDstFctn;

    public SharedBipartiteMatchingUtils(Network network) {
        accDstFctn = new NetworkMinTimeDistanceFunction(network, new FastAStarLandmarksFactory());
    }

    public Tensor executePickup(SharedUniversalDispatcher universalDispatcher, //
            Function<AVRequest, RoboTaxi> getPickupTaxi, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            DistanceFunction distanceFunction, Network network) {
        Tensor infoLine = Tensors.empty();

        Map<RoboTaxi, AVRequest> gbpMatchCleaned = getGBPMatch(getPickupTaxi, roboTaxis, requests, distanceFunction, network);

        /** perform dispatching */
        gbpMatchCleaned.forEach(universalDispatcher::addSharedRoboTaxiPickup);
        return infoLine; // TODO always empty?
    }

    public Map<RoboTaxi, AVRequest> getGBPMatch(Function<AVRequest, RoboTaxi> getPickupTaxi, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            DistanceFunction distanceFunction, Network network) {

        /** reduction of problem size with kd-tree, helps to downsize problems where n << m or m>> n
         * for n number of available taxis and m number of available requests */
        Map<RoboTaxi, AVRequest> gbpMatch = ((new GlobalBipartiteMatching(new DistanceCost(distanceFunction))).match(roboTaxis, requests));

        /** prevent cycling an assignment is only updated if the new distance is smaller than the
         * old distance */
        return CyclicSolutionPreventer.apply(gbpMatch, getPickupTaxi, accDstFctn);
    }
}
