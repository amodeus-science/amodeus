/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.virtualnetwork.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import amodeus.amodeus.util.math.GlobalAssert;
import amodeus.amodeus.util.math.IntPoint;
import ch.ethz.idsc.tensor.nrm.Vector2Norm;

/* package */ enum VirtualLinkBuilder {
    ;

    public static <T, U> void build( //
            VirtualNetwork<T> virtualNetwork, boolean completeGraph, Map<U, Set<T>> uElements) {
        if (completeGraph)
            VirtualLinkBuilder.buildComplete(virtualNetwork);
        else
            VirtualLinkBuilder.buildNeighboring(virtualNetwork, uElements);
    }

    /** on a network with virtualNodes this function builds a graph where every {@link VirtualNode} is
     * connected to every other {@link VirtualNode}
     * 
     * @param _virtualNetwork */
    public static <T> void buildComplete(VirtualNetwork<T> _virtualNetwork) {
        VirtualNetworkImpl<T> virtualNetwork = (VirtualNetworkImpl<T>) _virtualNetwork;
        GlobalAssert.that(virtualNetwork.getVirtualLinks().isEmpty());
        int index = 0;
        for (VirtualNode<T> vNfrom : virtualNetwork.getVirtualNodes())
            for (VirtualNode<T> vNto : virtualNetwork.getVirtualNodes())
                if (!vNfrom.equals(vNto)) {
                    String indexStr = "vLink_" + Integer.toString(index + 1);
                    virtualNetwork.addVirtualLink(indexStr, vNfrom, vNto, //
                            Vector2Norm.between(vNfrom.getCoord(), vNto.getCoord()).number().doubleValue());
                    ++index;
                }

    }

    /** @param _virtualNetwork without {@link VirtualLink}
     * @param uElements a {@link HashMap} with elements U that are associated to 1 or more T, two {@link VirtualNode}
     *            are neighboring if some U is associated to a {@link T} in both of them. */
    private static <T, U> void buildNeighboring(VirtualNetwork<T> _virtualNetwork, Map<U, Set<T>> uElements) {
        VirtualNetworkImpl<T> virtualNetwork = (VirtualNetworkImpl<T>) _virtualNetwork;
        GenericButterfliesAndRainbows<T, U> gbf = new GenericButterfliesAndRainbows<>();

        uElements.forEach((u, tSet) -> tSet.forEach(t -> gbf.add(u, virtualNetwork.getVirtualNode(t))));

        int index = 0;
        System.out.println("there are " + gbf.allPairs().size() + " virtualLinks.");
        for (IntPoint point : gbf.allPairs()) {
            VirtualNode<T> vNfrom = virtualNetwork.getVirtualNode(point.x);
            VirtualNode<T> vNto = virtualNetwork.getVirtualNode(point.y);
            String indexStr = "vLink_" + Integer.toString(index + 1);
            virtualNetwork.addVirtualLink(indexStr, vNfrom, vNto, //
            		Vector2Norm.between(vNfrom.getCoord(), vNto.getCoord()).number().doubleValue());
            ++index;
        }
    }
}
