package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Map;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.passenger.AVRequest;

public enum GlobalBipartiteMatching {
    ;

    public static Map<RoboTaxi, AVRequest> of(Collection<RoboTaxi> roboTaxis, Collection<AVRequest> requests, //
            DistanceFunction distanceFunction, Network network, Tensor infoLine, boolean reducewithKDTree) {

        if (reducewithKDTree == true && !(distanceFunction instanceof EuclideanDistanceFunction)) {
            System.err.println("cannot use requestReducing technique with other distance function than Euclidean.");
            GlobalAssert.that(false);
        }

        // save initial problem size
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

}
