/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import ch.ethz.idsc.amodeus.util.nd.NdCenterInterface;
import ch.ethz.idsc.amodeus.util.nd.NdCluster;
import ch.ethz.idsc.amodeus.util.nd.NdMap;
import ch.ethz.idsc.amodeus.util.nd.NdTreeMap;
import ch.ethz.idsc.tensor.Tensor;

//TODO Joel document each function since order of calls matters
//TODO Joel potentially change API (design as class) to avoid misuse 
//TODO Joel general refactor
public enum VirtualNetworkCreatorUtils {
    ;

    public static <T> void addToVNodes( //
            Map<VirtualNode<T>, Set<T>> vNMap, Function<T, String> nameOf, VirtualNetwork<T> _virtualNetwork) {
        VirtualNetworkImpl<T> virtualNetwork = (VirtualNetworkImpl<T>) _virtualNetwork;
        for (VirtualNode<T> virtualNode : vNMap.keySet()) {
            Map<String, T> map = new HashMap<>();
            vNMap.get(virtualNode).stream().forEach(l -> map.put(nameOf.apply(l), l));
            virtualNode.setLinks(map);
            virtualNetwork.addVirtualNode(virtualNode); // <- function requires the final set of links belonging to virtual node
        }
    }

    public static <T> void fillSerializationInfo( //
            Collection<T> elements, VirtualNetwork<T> _virtualNetwork, Function<T, String> nameOf) {

        VirtualNetworkImpl<T> virtualNetwork = (VirtualNetworkImpl<T>) _virtualNetwork;
        Map<T, String> map = new HashMap<>();
        elements.forEach(l -> map.put(l, nameOf.apply(l)));
        virtualNetwork.fillVNodeMapRAWVERYPRIVATE(map);
    }


}
