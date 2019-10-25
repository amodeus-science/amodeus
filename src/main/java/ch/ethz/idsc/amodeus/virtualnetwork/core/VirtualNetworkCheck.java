/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork.core;

import java.util.HashSet;

/* package */ enum VirtualNetworkCheck {
    ;

    public static <T> boolean virtualLinkConsistencyCheck(VirtualNetwork<T> virtualNetwork) {

        /** this ensures that every virtualNode -> virtualNode combination is only represented
         * by one virtualLink */

        int numVirtualLinks = virtualNetwork.getvLinksCount();

        HashSet<String> ids = new HashSet<>();
        for (VirtualLink<T> virtualLink : virtualNetwork.getVirtualLinks()) {
            VirtualNode<T> from = virtualLink.getFrom();
            VirtualNode<T> to = virtualLink.getTo();
            String uniqueString = from.getId().toString() + "-" + to.getId().toString();
            ids.add(uniqueString);
        }

        /** if the below equality does not hold, then there must be a
         * duplicate vNode -> vNode in some of the vLinks */
        return (ids.size() == numVirtualLinks);
    }
}
