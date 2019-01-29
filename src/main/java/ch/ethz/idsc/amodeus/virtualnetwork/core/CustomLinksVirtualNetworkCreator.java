package ch.ethz.idsc.amodeus.virtualnetwork.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Objects;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.red.Norm;

public abstract class CustomLinksVirtualNetworkCreator<T, U> extends AbstractVirtualNetworkCreator<T, U> {

    private final Map<VirtualNode<T>, Set<VirtualNode<T>>> customLinks = new HashMap<>();

    public void planLink(VirtualNode<T> vN1, VirtualNode<T> vN2) {
        if (!Objects.isNull(vN1) && !Objects.isNull(vN2)) {
            if (customLinks.containsKey(vN1)) {
                if (customLinks.containsKey(vN2)) 
                    GlobalAssert.that(!customLinks.get(vN2).contains(vN2));
                add(vN1, vN2);
            } else if (customLinks.containsKey(vN2)) {
                add(vN2, vN1);
            } else {
                customLinks.put(vN1, new HashSet<>());
                add(vN1, vN2);
            }
        }
    }

    private void add(VirtualNode<T> vN1, VirtualNode<T> vN2) {
        customLinks.get(vN1).add(vN2);
    }

    @Override
    protected void buildLinks(VirtualNetwork<T> virtualNetwork, boolean completeGraph, Map<U, HashSet<T>> uElements) {
        GlobalAssert.that(virtualNetwork.getVirtualLinks().isEmpty());
        VirtualNetworkImpl<T> virtualNetworkImpl = (VirtualNetworkImpl<T>) virtualNetwork;
        int index = 0;
        for (Entry<VirtualNode<T>, Set<VirtualNode<T>>> customLink : customLinks.entrySet()) {
            VirtualNode<T> vNfrom = customLink.getKey();
            for (VirtualNode<T> vNto : customLink.getValue()) {
                String indexStr = "vLink_" + Integer.toString(index + 1);
                virtualNetworkImpl.addVirtualLink(indexStr, vNfrom, vNto, //
                        Norm._2.between(vNfrom.getCoord(), vNto.getCoord()).number().doubleValue());
                index++;
            }
        }
    }
}
