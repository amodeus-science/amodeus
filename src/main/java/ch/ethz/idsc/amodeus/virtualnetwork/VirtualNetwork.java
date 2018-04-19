/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.matsim.api.core.v01.network.Link;

/** A VirtualNetwork<T> is a network consisting of VirtualNodes that are connected by VirtualLinks and every
 * nodes has a set of distinct T elements that belong to it.
 * 
 * @param <T> */
public interface VirtualNetwork<T> {

    /** @return {@link Collection} of all {@link VirtualNode} that belong to the {@link VirtualNetwork} */
    Collection<VirtualNode<T>> getVirtualNodes();

    /** @return {@link Collection} of all {@link VirtualLink} that belong to the {@link VirtualNetwork} */
    Collection<VirtualLink<T>> getVirtualLinks();

    /** @return number of {@link VirtualNode} */
    int getvNodesCount();

    /** @return number of {@link VirtualLink} */
    int getvLinksCount();

    /** @param element {@link T} of VirtualNetwork
     * @return {@link VirtualNode } that @param element belongs to */
    VirtualNode<T> getVirtualNode(T element);

    /** @param index
     * @return {@link VirtualLink} with index @param index */
    VirtualLink<T> getVirtualLink(int index);

    /** @param index
     * @return {@link VirtualNode} with index @param index */
    VirtualNode<T> getVirtualNode(int index);

    /** @param fromIn
     * @param toIn
     * @return VirtualLink object between fromIn and toIn, or null if such a VirtualLink is not defined */
    VirtualLink<T> getVirtualLink(VirtualNode<T> fromIn, VirtualNode<T> toIn);

    /** @param fromIn
     * @param toIn
     * @return true if VirtualLink object between fromIn and toIn is defined */
    boolean containsVirtualLink(VirtualNode<T> fromIn, VirtualNode<T> toIn);

    /** @return return virtualNode related HashMaps */
    <U> Map<VirtualNode<T>, List<U>> createVNodeTypeMap();

    /** @param col {@link Collection} of objects associated to a {@link Link}
     * @param function bijection linking a object from @param col to a {@link Link}
     * @return {@link java.util.Map} where all objects in @param col are sorted at {@link VirtualNode} that the link supplied by @param function belongs to */
    <U> Map<VirtualNode<T>, List<U>> binToVirtualNode(Collection<U> col, Function<U, T> function);

    /** Completes the missing references after the virtualNetwork was read from a serialized bitmap.**
     * 
     * @param network */
    void fillSerializationInfo(Map<String, T> map);

    void checkConsistency();

    long getvNetworkID();

    void printVirtualNetworkInfo();

}
