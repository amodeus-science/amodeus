/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork;

enum VirtualNetworkCheck {
    ;

    public static <T> boolean virtualLinkConsistencyCheck(VirtualNetwork<T> virtualNetwork) {
        boolean networkIsConsistent = true;

        // every VN -> VN can only represent one virtual Link
        for (VirtualLink<T> virtualLink : virtualNetwork.getVirtualLinks()) {
            VirtualNode<T> from = virtualLink.getFrom();
            VirtualNode<T> to = virtualLink.getTo();

            for (VirtualLink<T> virtualLinkOther : virtualNetwork.getVirtualLinks()) {
                if (!virtualLink.equals(virtualLinkOther)) {
                    boolean sameFrom = virtualLinkOther.getFrom().equals(from);
                    boolean sameTo = virtualLinkOther.getTo().equals(to);
                    if ((sameFrom && sameTo)) {
                        System.out.println("virtualLink " + virtualLink.getId() + " is from " + virtualLink.getFrom().getId() + " to " + virtualLink.getTo().getId());
                        System.out.println("virtualLink " + virtualLinkOther.getId() + " is from " + virtualLinkOther.getFrom().getId() + //
                                " to " + virtualLinkOther.getTo().getId());
                        networkIsConsistent = false;
                        break;
                    }
                }
            }
        }

        return networkIsConsistent;
    }

}
