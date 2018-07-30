/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.nd.NdCenterInterface;
import ch.ethz.idsc.amodeus.util.nd.NdCluster;
import ch.ethz.idsc.amodeus.util.nd.NdMap;
import ch.ethz.idsc.amodeus.util.nd.NdTreeMap;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ enum KdTreeReducerHelper {
    ;

    /** @param requests @param roboTaxis @param network
     * @return minimal cardinality reduced {@link Collection} of {@link AVRequest}s which has to be considered
     *         in a global bipartite matching problem to find the optimal matching, reduction is done with Nd-Tree */
    public static Collection<AVRequest> reduceRequests(Collection<AVRequest> requests, Collection<RoboTaxi> roboTaxis, Network network) {
        Tensor bounds = NetworkBounds.of(network);
        Tensor lbounds = bounds.get(0);
        Tensor ubounds = bounds.get(1);
        return reduceReductible(requests, roboTaxis, TensorLocation::of, TensorLocation::of, lbounds, ubounds);
    }

    /** @param requests @param roboTaxis @param network
     * @return minimal cardinality reduced {@link Collection} of {@link RoboTaxi}s which has to be considered
     *         in a global bipartite matching problem to find the optimal matching, reduction is done with Nd-Tree */
    public static Collection<RoboTaxi> reduceRoboTaxis(Collection<AVRequest> requests, Collection<RoboTaxi> roboTaxis, //
            Network network) {
        Tensor bounds = NetworkBounds.of(network);
        Tensor lbounds = bounds.get(0);
        Tensor ubounds = bounds.get(1);
        return reduceReductible(roboTaxis, requests, TensorLocation::of, TensorLocation::of, lbounds, ubounds);
    }

    /** @param reductibleCol {@link Collection} which should be reduced
     * @param invariantCol {@link Collection} which is invariant
     * @param t1Location location of an element T1 in the plane
     * @param t2Location location of an element T2 in the plane
     * @param lbounds lower bounds of the network
     * @param ubounds upper bounds of the network
     * @return */
    private static <T1, T2> Collection<T1> reduceReductible(Collection<T1> reductibleCol, Collection<T2> invariantCol, //
            Function<T1, Tensor> t1Location, Function<T2, Tensor> t2Location, Tensor lbounds, Tensor ubounds) {
        /** reductibleCollection smaller than invariantCollection, no change */
        if (reductibleCol.size() < invariantCol.size() || reductibleCol.size() < 10)
            return reductibleCol;

        /** building the nd Tree */
        // Tensor bounds = NetworkBounds.of(network);
        // Tensor lbounds = bounds.get(0);
        // Tensor ubounds = bounds.get(1);
        NdMap<T1> ndTree = new NdTreeMap<>(lbounds, ubounds, 10, 24);

        /** add elements of reductible collection to nd-tree */
        for (T1 t1 : reductibleCol) {
            ndTree.add(t1Location.apply(t1), t1);
        }

        /** for all elements of the invariantCollection, start nearestNeighborSearch on elements of reductibleCollection
         * until union as large as the cardinality of the invariantCol */
        Collection<T1> t1Chosen = new HashSet<>();
        int iter = 1;
        do {
            t1Chosen.clear();
            for (T2 t2 : invariantCol) {
                Tensor center = t2Location.apply(t2);
                NdCluster<T1> nearestCluster = ndTree.buildCluster(NdCenterInterface.euclidean(center), iter);
                nearestCluster.stream().forEach(ndentry -> t1Chosen.add(ndentry.value()));
            }
            ++iter;
        } while (t1Chosen.size() < invariantCol.size() && iter <= invariantCol.size());

        return t1Chosen;
    }
}
