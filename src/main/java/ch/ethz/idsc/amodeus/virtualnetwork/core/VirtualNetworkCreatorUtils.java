/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork.core;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/* package */ enum VirtualNetworkCreatorUtils {
    ;

    public static <T> void addToVNodes(Map<VirtualNode<T>, Set<T>> vNMap, Function<T, String> nameOf, //
            VirtualNetwork<T> _virtualNetwork) {
        VirtualNetworkImpl<T> virtualNetwork = (VirtualNetworkImpl<T>) _virtualNetwork;
        for (VirtualNode<T> virtualNode : vNMap.keySet()) {
            Map<String, T> map = vNMap.get(virtualNode).stream().collect(Collectors.toMap(nameOf, Function.identity()));
            virtualNode.setLinks(map);
            virtualNetwork.addVirtualNode(virtualNode); // <- function requires the final set of links belonging to virtual node
        }
    }

    public static <T> void fillSerializationInfo(Collection<T> elements, VirtualNetwork<T> _virtualNetwork, //
            Function<T, String> nameOf) {
        VirtualNetworkImpl<T> virtualNetwork = (VirtualNetworkImpl<T>) _virtualNetwork;
        Map<T, String> map = elements.stream().collect(Collectors.toMap(Function.identity(), nameOf));
        virtualNetwork.fillVNodeMapRAWVERYPRIVATE(map);
    }
}
