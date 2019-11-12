/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork.core;

/* package */ enum VirtualNetworkCheck {
    ;

    public static <T> boolean virtualLinkConsistencyCheck(VirtualNetwork<T> virtualNetwork) {

        /** this ensures that every virtualNode -> virtualNode combination is only represented
         * by one virtualLink */

        int numVirtualLinks = virtualNetwork.getvLinksCount();

        long numCombinations = virtualNetwork.getVirtualLinks().stream().map(virtualLink -> {
            VirtualNode<T> from = virtualLink.getFrom();
            VirtualNode<T> to = virtualLink.getTo();
            return from.getId() + "-" + to.getId();
        }).distinct().count();

        /** if the below equality does not hold, then there must be a
         * duplicate vNode -> vNode in some of the vLinks */
        return (numCombinations == numVirtualLinks);
    }
}
