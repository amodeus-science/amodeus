/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.drss;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.prep.MatsimNeighbourRectangleVirtualNetworkCreator;
import ch.ethz.idsc.amodeus.prep.MatsimRectangleVirtualNetworkCreator;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;

public class GridRebalancing {

    private final VirtualNetwork<Link> virtualNetwork;
    
    public GridRebalancing(Network network, double gridDistance, boolean completeGraph) {
        int[] ns = NumberCellsCalculator.of(network, gridDistance);
        virtualNetwork = MatsimNeighbourRectangleVirtualNetworkCreator.createVirtualNetwork(network, ns[0], ns[1]);
    }
    
    
}
