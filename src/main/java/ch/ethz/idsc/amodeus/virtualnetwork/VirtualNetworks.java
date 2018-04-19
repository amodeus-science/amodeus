package ch.ethz.idsc.amodeus.virtualnetwork;

public enum VirtualNetworks {
    ;
    /** @param element {@link T} of VirtualNetwork
     * @return Check if @param element is included in the VirtualNetwork */
    public static <T> boolean hasNodeFor(VirtualNetwork<T> virtualNetwork, T element) {
        return ((VirtualNetworkImpl<T>) virtualNetwork).networkElements.containsKey(element);
    }
}
