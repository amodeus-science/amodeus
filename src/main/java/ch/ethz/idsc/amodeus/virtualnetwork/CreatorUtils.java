/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import ch.ethz.idsc.amodeus.util.nd.NdCenterInterface;
import ch.ethz.idsc.amodeus.util.nd.NdCluster;
import ch.ethz.idsc.amodeus.util.nd.NdTreeMap;
import ch.ethz.idsc.tensor.Tensor;

public enum CreatorUtils {
    ;

    public static <T> void addToVNodes(Map<VirtualNode<T>, Set<T>> vNMap, Function<T, String> nameOf, VirtualNetwork<T> _virtualNetwork) {
        VirtualNetworkImpl<T> virtualNetwork = (VirtualNetworkImpl<T>) _virtualNetwork;
        for (VirtualNode<T> virtualNode : vNMap.keySet()) {
            HashMap<String, T> map = new HashMap<>();
            vNMap.get(virtualNode).stream().forEach(l -> map.put(nameOf.apply(l), l));
            virtualNode.setLinks(map);
            virtualNetwork.addVirtualNode(virtualNode); // <- function requires the final set of links belonging to virtual node
        }
    }

    public static <T> void fillSerializationInfo(Collection<T> elements, //
            VirtualNetwork<T> _virtualNetwork, Function<T, String> nameOf) {
        VirtualNetworkImpl<T> virtualNetwork = (VirtualNetworkImpl<T>) _virtualNetwork;
        Map<T, String> map = new HashMap<>();
        elements.forEach(l -> map.put(l, nameOf.apply(l)));
        virtualNetwork.fillVNodeMapRAWVERYPRIVATE(map);

    }

    public static <T> void addByProximity(Map<VirtualNode<T>, Set<T>> vNMap, //
            Tensor lbounds, Tensor ubounds, Collection<T> elements, Function<T, Tensor> locationOf) {
        NdTreeMap<VirtualNode<T>> ndTree = new NdTreeMap<>(lbounds, ubounds, 10, 24);

        for (VirtualNode<T> virtualNode : vNMap.keySet()) {
            ndTree.add(virtualNode.getCoord(), virtualNode);
        }

        // associate link to virtual node based on proximity to voronoi center
        for (T t : elements) {
            NdCluster<VirtualNode<T>> closestNodeC = ndTree.buildCluster(NdCenterInterface.euclidean(locationOf.apply(t)), 1);
            VirtualNode<T> closestNode = closestNodeC.stream().findAny().get().value();
            vNMap.get(closestNode).add(t);
        }

    }

}
