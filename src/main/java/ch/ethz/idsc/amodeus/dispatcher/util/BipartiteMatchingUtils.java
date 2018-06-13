/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.UniversalDispatcher;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.passenger.AVRequest;

public enum BipartiteMatchingUtils {
    ;

    public static Tensor executePickup( //
            UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, // <- typically universalDispatcher.getDivertableRoboTaxis()
            Collection<AVRequest> requests, //
            DistanceFunction distanceFunction, //
            Network network, //
            boolean reducewithKDTree) {

        Tensor infoLine = Tensors.empty();
        Map<RoboTaxi, AVRequest> gbpMatch = globalBipartiteMatching(roboTaxis, requests, distanceFunction, network, infoLine, reducewithKDTree);

        if (distanceFunction instanceof NonCyclicDistanceFunction) {
            DistanceFunction accDistanceFunction = ((NonCyclicDistanceFunction) distanceFunction).cyclicSolutionPreventer;
            removeCyclicSolutions(universalDispatcher, accDistanceFunction, gbpMatch);
        }

        for (Entry<RoboTaxi, AVRequest> entry : gbpMatch.entrySet())
            universalDispatcher.setRoboTaxiPickup(entry.getKey(), entry.getValue());

        return infoLine;
    }

    public static Tensor executeRebalance(BiConsumer<RoboTaxi, Link> setFunction, Collection<RoboTaxi> roboTaxis, Collection<AVRequest> requests, //
            DistanceFunction distanceFunction, Network network, boolean reducewithKDTree) {
        Tensor infoLine = Tensors.empty();
        Map<RoboTaxi, AVRequest> gbpMatch = globalBipartiteMatching(roboTaxis, requests, distanceFunction, network, infoLine, reducewithKDTree);
        for (Entry<RoboTaxi, AVRequest> entry : gbpMatch.entrySet()) {
            setFunction.accept(entry.getKey(), entry.getValue().getFromLink());
        }
        return infoLine;
    }

    private static Map<RoboTaxi, AVRequest> globalBipartiteMatching(Collection<RoboTaxi> roboTaxis, Collection<AVRequest> requests, //
            DistanceFunction distanceFunction, Network network, Tensor infoLine, boolean reducewithKDTree) {

        if (reducewithKDTree == true && !(distanceFunction instanceof EuclideanDistanceFunction)) {
            System.err.println("cannot use requestReducing technique with other distance function than Euclidean.");
            GlobalAssert.that(false);
        }

        // save initial problemsize
        infoLine.append(Tensors.vectorInt(roboTaxis.size(), requests.size()));

        if (reducewithKDTree) {
            // 1) In case roboTaxis >> requests reduce search space using kd-trees
            Collection<RoboTaxi> roboTaxisReduced = StaticHelper.reduceRoboTaxis(requests, roboTaxis, network);

            // 2) In case requests >> roboTaxis reduce the search space using kd-trees
            Collection<AVRequest> requestsReduced = StaticHelper.reduceRequests(requests, roboTaxis, network);

            // 3) compute Euclidean bipartite matching for all vehicles using the Hungarian method and set new pickup commands
            infoLine.append(Tensors.vectorInt(roboTaxisReduced.size(), requestsReduced.size()));

            return ((new HungarBiPartVehicleDestMatcher(//
                    distanceFunction)).matchAVRequest(roboTaxisReduced, requestsReduced));
        }
        return ((new HungarBiPartVehicleDestMatcher(//
                distanceFunction)).matchAVRequest(roboTaxis, requests));
    }

    private static final double MARGIN_EPS = 1e-8;

    private static void removeCyclicSolutions(UniversalDispatcher universalDispatcher, DistanceFunction accDistanceFunction, Map<RoboTaxi, AVRequest> taxiToAV) {
        Map<RoboTaxi, AVRequest> copyTaxiToAV = new HashMap<>(taxiToAV);

        for (Entry<RoboTaxi, AVRequest> entry : copyTaxiToAV.entrySet()) {
            Optional<RoboTaxi> optional = universalDispatcher.getPickupTaxi(entry.getValue()); // previously assigned taxi
            if (optional.isPresent()) { // only do comparison if request has taxi assigned
                final RoboTaxi oldTaxi = optional.get(); // current pickup taxi of request
                final RoboTaxi newTaxi = entry.getKey(); // candidate
                GlobalAssert.that(Objects.nonNull(newTaxi));
                if (newTaxi != oldTaxi) { // only consider changed taxi assignments
                    double distNew = accDistanceFunction.getDistance(newTaxi, entry.getValue());
                    double distOld = accDistanceFunction.getDistance(oldTaxi, entry.getValue());
                    if (distNew + MARGIN_EPS >= distOld) {
                        // prevent new assignment when the new taxi is not closer in network distance AND
                        // (the previously assigned taxi is either assigned to a request further away or to none anymore)
                        // if (copyTaxiToAV.get(asgndTaxi) == null) || accDistanceFunction.getDistance(asgndTaxi, copyTaxiToAV.get(asgndTaxi)) > distAss) {
                        taxiToAV.remove(newTaxi);
                        // }
                    }
                }
            }
        }
    }

}
