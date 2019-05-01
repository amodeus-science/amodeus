/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public abstract class AbstractVirtualNetworkCreator<T, U> {

    protected VirtualNetwork<T> virtualNetwork;

    protected VirtualNetwork<T> createVirtualNetwork(Map<VirtualNode<T>, Set<T>> vNodeTMap, //
            Collection<T> elements, Map<U, HashSet<T>> uElements, Function<T, String> nameOf, //
            boolean completeGraph) {

        /** initialize new {@link VirtualNetwork} */
        VirtualNetwork<T> virtualNetwork = new VirtualNetworkImpl<>();

        VirtualNetworkCreatorUtils.addToVNodes(vNodeTMap, nameOf, virtualNetwork);

        /** create virtualLinks for complete or neighboring graph */
        VirtualLinkBuilder.build(virtualNetwork, completeGraph, uElements);
        GlobalAssert.that(VirtualNetworkCheck.virtualLinkConsistencyCheck(virtualNetwork));

        /** fill information for serialization */
        VirtualNetworkCreatorUtils.fillSerializationInfo(elements, virtualNetwork, nameOf);

        return virtualNetwork;

    }

    public VirtualNetwork<T> getVirtualNetwork() {
        return virtualNetwork;
    }
}
