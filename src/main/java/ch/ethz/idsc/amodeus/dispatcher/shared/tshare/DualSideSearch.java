package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.FastAStarLandmarksFactory;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.util.NetworkDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.NetworkMinTimeDistanceFunction;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.matsim.av.passenger.AVRequest;

public class DualSideSearch {

    private final Map<VirtualNode<Link>, GridCell> gridCells;
    private final VirtualNetwork<Link> virtualNetwork;
    private final double maxPickupDelay;
    private final double maxDrpoffDelay;
    private final NetworkDistanceFunction distance;

    public DualSideSearch(Map<VirtualNode<Link>, GridCell> gridCells, VirtualNetwork<Link> virtualNetwork, //
            double maxPickupDelay, double maxDrpoffDelay, Network network) {
        this.distance = new NetworkMinTimeDistanceFunction(network, new FastAStarLandmarksFactory());
        this.virtualNetwork = virtualNetwork;
        this.gridCells = gridCells;
        this.maxPickupDelay = maxPickupDelay;
        this.maxDrpoffDelay = maxDrpoffDelay;
    }

    public Collection<RoboTaxi> apply(AVRequest request) {
        double latestPickup = request.getSubmissionTime() + maxPickupDelay;
        double latestArrval = distance.getTravelTime(request.getFromLink(), request.getToLink())//
                + maxDrpoffDelay;

        GridCell oCell = gridCells.get(virtualNetwork.getVirtualNode(request.getToLink()));
        GridCell dCell = gridCells.get(virtualNetwork.getVirtualNode(request.getFromLink()));

        Collection<RoboTaxi> oTaxis = new ArrayList<>();
        Collection<RoboTaxi> dTaxis = new ArrayList<>();
        Collection<RoboTaxi> potentialTaxis = new ArrayList<>();

        List<VirtualNode<Link>> oCloseCells = StaticHelper.getAllWithinLessThan(latestPickup, oCell, virtualNetwork);
        List<VirtualNode<Link>> dCloseCells = StaticHelper.getAllWithinLessThan(latestArrval, dCell, virtualNetwork);

        // FIXME continue here 
        return null;
    }
}
