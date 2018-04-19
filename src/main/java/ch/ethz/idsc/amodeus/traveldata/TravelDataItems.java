package ch.ethz.idsc.amodeus.traveldata;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetworks;

public enum TravelDataItems {
    ;
    public static boolean isContained(VirtualNetwork<Link> virtualNetwork, TravelDataItem item) {
        return VirtualNetworks.hasNodeFor(virtualNetwork, item.startLink) && //
                VirtualNetworks.hasNodeFor(virtualNetwork, item.endLink);
    }

}
