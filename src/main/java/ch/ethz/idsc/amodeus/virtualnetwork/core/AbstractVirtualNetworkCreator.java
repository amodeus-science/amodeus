/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork.core;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public abstract class AbstractVirtualNetworkCreator<T, U> {

    protected VirtualNetwork<T> virtualNetwork;

    public VirtualNetwork<T> getVirtualNetwork() {
        return virtualNetwork;
    }

    /** @return a {@link VirtualNetwork}, children classes must call this function to create a
     *         {@link VirtualNetwork}, the following inputs are needed:
     * 
     *         - a {@link Map} @param vNodeTMap that assigns all the elements in the network T to a certain
     *         {@link VirtualNode}
     *         - the {@link Collection} @param elements of all link elements T in the network
     *         - the {@link Map} @param uElements of all node elements U that assigns all link elements T adjacent to the
     *         node U to it.
     *         - A {@link Function} @param nameOf assigning a {@link String} name to each element T
     *         - A {@link Boolean} @param completeGraph that encodes if all possible links between
     *         {@link VirtualNode}s should be introduced or only the {@link VirtualLink}s between neighboring
     *         {@link VirtualNode}s */
    protected VirtualNetwork<T> createVirtualNetwork(Map<VirtualNode<T>, Set<T>> vNodeTMap, //
            Collection<T> elements, Map<U, Set<T>> uElements, Function<T, String> nameOf, //
            boolean completeGraph) {

        /** initialize new {@link VirtualNetwork} */
        VirtualNetwork<T> virtualNetwork = new VirtualNetworkImpl<>();

        /** assign link elements to virtual nodes */
        VirtualNetworkCreatorUtils.addToVNodes(vNodeTMap, nameOf, virtualNetwork);

        /** create virtualLinks for complete or neighboring graph */
        VirtualLinkBuilder.build(virtualNetwork, completeGraph, uElements);
        GlobalAssert.that(VirtualNetworkCheck.virtualLinkConsistencyCheck(virtualNetwork));

        /** fill information for serialization */
        VirtualNetworkCreatorUtils.fillSerializationInfo(elements, virtualNetwork, nameOf);

        return virtualNetwork;

    }

}
