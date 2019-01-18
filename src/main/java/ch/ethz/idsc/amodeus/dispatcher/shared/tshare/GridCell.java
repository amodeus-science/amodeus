package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.idsc.amodeus.dispatcher.util.NetworkDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.NetworkMinDistDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.NetworkMinTimeDistanceFunction;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;

/* package */ class GridCell {

    private final VirtualNode<Link> myVNode;
    private final NavigableMap<Double, VirtualNode<Link>> temporalMap = new TreeMap<>();
    private final NavigableMap<Double, VirtualNode<Link>> distanceMap = new TreeMap<>();
    private final NetworkDistanceFunction minDist;
    private final NetworkDistanceFunction minTime;

    public GridCell(VirtualNode<Link> virtualNode, VirtualNetwork<Link> virtualNetwork, Network network) {
        this.myVNode = virtualNode;
        this.minTime = new NetworkMinTimeDistanceFunction(network, new FastAStarLandmarksFactory());
        this.minDist = new NetworkMinDistDistanceFunction(network, new FastAStarLandmarksFactory());
        computeMaps(virtualNetwork, FastQuadTree.of(network));
    }

    private void computeMaps(VirtualNetwork<Link> virtualNetwork, QuadTree<Link> links) {
        for (VirtualNode<Link> toNode : virtualNetwork.getVirtualNodes()) {
            Link fromLink = links.getClosest(//
                    myVNode.getCoord().Get(0).number().doubleValue(), //
                    myVNode.getCoord().Get(1).number().doubleValue());
            Link toLink = links.getClosest(//
                    toNode.getCoord().Get(0).number().doubleValue(), //
                    toNode.getCoord().Get(1).number().doubleValue());
            double time = minTime.getTravelTime(fromLink, toLink);
            double distance = minDist.getDistance(fromLink, toLink);
            temporalMap.put(time, toNode);
            distanceMap.put(distance, toNode);
        }
    }

    public List<VirtualNode<Link>> getDistClosest(int n) {
        return StaticHelper.getSortedClosest(n, distanceMap);
    }

    public List<VirtualNode<Link>> getTimeClosest(int n) {
        return StaticHelper.getSortedClosest(n, temporalMap);
    }
}
