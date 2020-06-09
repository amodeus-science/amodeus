/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.util;

import java.util.List;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.virtualnetwork.core.VirtualNode;

@FunctionalInterface
public interface AbstractVirtualNodeDest {
    /** @return for the {@link VirtualNode} @param virtualNode return @param size
     *         {@link Link}s contained in @param virtualNode */
    List<Link> selectLinkSet(VirtualNode<Link> virtualNode, int size);
}
