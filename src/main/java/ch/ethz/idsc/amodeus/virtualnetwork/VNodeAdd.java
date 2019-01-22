package ch.ethz.idsc.amodeus.virtualnetwork;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import ch.ethz.idsc.amodeus.util.nd.NdCenterInterface;
import ch.ethz.idsc.amodeus.util.nd.NdCluster;
import ch.ethz.idsc.amodeus.util.nd.NdMap;
import ch.ethz.idsc.amodeus.util.nd.NdTreeMap;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.Tensor;

public enum VNodeAdd {
    ;

    /** Takes @param vNMap and associates all the T in @param elements to the closest key in the map.
     * 
     * @param lbounds
     * @param ubounds
     * @param locationOf */
    public static <T> void byProximity(Map<VirtualNode<T>, Set<T>> vNMap, //
            Tensor lbounds, Tensor ubounds, Collection<T> elements, Function<T, Tensor> locationOf) {
        NdMap<VirtualNode<T>> ndMap = new NdTreeMap<>(lbounds, ubounds, 10, 24);
        /** add virtual nodes to a NdTree */
        for (VirtualNode<T> virtualNode : vNMap.keySet())
            ndMap.add(virtualNode.getCoord(), virtualNode);
        /** associate link to virtual node based on proximity to voronoi center */
        for (T t : elements) {
            NdCluster<VirtualNode<T>> closestNodeC = ndMap.buildCluster(NdCenterInterface.euclidean(locationOf.apply(t)), 1);
            VirtualNode<T> closestNode = closestNodeC.stream().findAny().get().value();
            vNMap.get(closestNode).add(t);
        }
    }
}
