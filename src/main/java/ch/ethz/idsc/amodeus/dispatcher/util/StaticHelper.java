/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.nd.NdCenterInterface;
import ch.ethz.idsc.amodeus.util.nd.NdCluster;
import ch.ethz.idsc.amodeus.util.nd.NdMap;
import ch.ethz.idsc.amodeus.util.nd.NdTreeMap;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ enum StaticHelper {
    ;

    static Collection<AVRequest> reduceRequests(Collection<AVRequest> requests, Collection<RoboTaxi> roboTaxis, Network network) {

        // for less requests than cars, don't do anything
        if (requests.size() < roboTaxis.size() || requests.size() < 10)
            return requests;

        // otherwise create Quadtree and return minimum amount of RoboTaxis
        // Build the ND tree
        Tensor bounds = NetworkBounds.of(network);
        Tensor lbounds = bounds.get(0);
        Tensor ubounds = bounds.get(1);

        NdMap<AVRequest> ndTree = new NdTreeMap<>(lbounds, ubounds, 10, 24);

        // add uniquely identifiable requests to KD tree
        for (AVRequest avRequest : requests) {
            ndTree.add(TensorLocation.of(avRequest), avRequest);
        }

        // for all roboTaxis vehicles, start nearestNeighborSearch until union is as large as the
        // number of vehicles start with only one request per vehicle
        Collection<AVRequest> requestsChosen = new HashSet<>();
        int iter = 1;
        do {
            requestsChosen.clear();
            for (RoboTaxi roboTaxi : roboTaxis) {
                Tensor center = TensorLocation.of(roboTaxi);
                NdCluster<AVRequest> nearestCluster = ndTree.buildCluster(NdCenterInterface.euclidean(center), iter);
                nearestCluster.stream().forEach(ndentry -> requestsChosen.add(ndentry.value()));
            }
            ++iter;
        } while (requestsChosen.size() < roboTaxis.size() && iter <= roboTaxis.size());

        return requestsChosen;

    }

    static Collection<RoboTaxi> reduceRoboTaxis(Collection<AVRequest> requests, Collection<RoboTaxi> roboTaxis, //
            Network network) {
        // for less requests than cars, don't do anything
        if (roboTaxis.size() < requests.size() || roboTaxis.size() < 10)
            return roboTaxis;

        // otherwise create Quadtree and return minimum amount of RoboTaxis
        // Build the ND tree
        Tensor bounds = NetworkBounds.of(network);
        Tensor lbounds = bounds.get(0);
        Tensor ubounds = bounds.get(1);

        NdMap<RoboTaxi> ndTree = new NdTreeMap<>(lbounds, ubounds, 10, 24);

        // add roboTaxis to ND Tree
        for (RoboTaxi robotaxi : roboTaxis) {
            ndTree.add(TensorLocation.of(robotaxi), robotaxi);
        }

        // for all robotaxis, start nearestNeighborSearch until union is as large as the number of requests
        // start with only one vehicle per request
        Set<RoboTaxi> vehiclesChosen = new HashSet<>(); // note: must be HashSet to avoid duplicate elements.
        int roboTaxiPerRequest = 1;
        do {
            vehiclesChosen.clear();
            for (AVRequest avRequest : requests) {
                Tensor center = TensorLocation.of(avRequest);
                NdCluster<RoboTaxi> nearestCluster = ndTree.buildCluster(NdCenterInterface.euclidean(center), roboTaxiPerRequest);
                nearestCluster.stream().forEach(ndentry -> vehiclesChosen.add(ndentry.value()));
            }
            ++roboTaxiPerRequest;
        } while (vehiclesChosen.size() < requests.size() && roboTaxiPerRequest <= requests.size());

        return vehiclesChosen;
    }

}
