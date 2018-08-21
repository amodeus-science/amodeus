/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.red.Norm;

public enum VirtualLinkBuilder {
    ;

    public static <T, U> void build(VirtualNetwork<T> virtualNetwork, boolean completeGraph, //
            Map<U, HashSet<T>> uElements) {
        if (completeGraph)
            VirtualLinkBuilder.buildComplete(virtualNetwork);
        else
            VirtualLinkBuilder.buildNeighboring(virtualNetwork, uElements);
    }

    /** on a network with virtualNodes this function builds a graph where every {@link VirtualNode} is
     * connected to every other {@link VirtualNode}
     * 
     * @param virtualNetwork */
    public static <T> void buildComplete(VirtualNetwork<T> _virtualNetwork) {
        VirtualNetworkImpl<T> virtualNetwork = (VirtualNetworkImpl<T>) _virtualNetwork;
        GlobalAssert.that(virtualNetwork.getVirtualLinks().isEmpty());
        int index = 0;
        for (VirtualNode<T> vNfrom : virtualNetwork.getVirtualNodes()) {
            for (VirtualNode<T> vNto : virtualNetwork.getVirtualNodes()) {
                if (!vNfrom.equals(vNto)) {
                    String indexStr = "vLink_" + Integer.toString(index + 1);
                    virtualNetwork.addVirtualLink(indexStr, vNfrom, vNto, //
                            Norm._2.between(vNfrom.getCoord(), vNto.getCoord()).number().doubleValue());
                    index++;
                }
            }
        }
    }

    /** @param virtualNetwork without {@link VirtualLink}
     * @param uElements a {@link HashMap} with elements U that are associated to 1 or more T, two {@link VirtualNode}
     *            are neighboring if some U is associated to a {@link T} in both of them. */
    private static <T, U> void buildNeighboring(VirtualNetwork<T> _virtualNetwork, Map<U, HashSet<T>> uElements) {
        VirtualNetworkImpl<T> virtualNetwork = (VirtualNetworkImpl<T>) _virtualNetwork;
        GenericButterfliesAndRainbows<T, U> gbf = new GenericButterfliesAndRainbows<>();

        for (U u : uElements.keySet()) {
            for (T t : uElements.get(u)) {
                gbf.add(u, virtualNetwork.getVirtualNode(t));
            }
        }

        int index = 0;
        System.out.println("there are " + gbf.allPairs().size() + " virtualLinks.");
        for (Point point : gbf.allPairs()) {
            VirtualNode<T> vNfrom = virtualNetwork.getVirtualNode(point.x);
            VirtualNode<T> vNto = virtualNetwork.getVirtualNode(point.y);
            String indexStr = "vLink_" + Integer.toString(index + 1);
            virtualNetwork.addVirtualLink(indexStr, vNfrom, vNto, //
                    Norm._2.between(vNfrom.getCoord(), vNto.getCoord()).number().doubleValue());
            index++;

        }
    }
}
