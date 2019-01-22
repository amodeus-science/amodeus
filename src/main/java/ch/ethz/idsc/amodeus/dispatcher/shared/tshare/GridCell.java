package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.idsc.amodeus.dispatcher.util.NetworkDistanceFunction;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;

/* package */ class GridCell {

    private final VirtualNode<Link> myVNode;
    private final NavigableMap<Double, VirtualNode<Link>> temporalSortedMap = new TreeMap<>();
    private final NavigableMap<Double, VirtualNode<Link>> distanceSortedMap = new TreeMap<>();

    private final Map<VirtualNode<Link>, Double> temporalLookupMap = new HashMap<>();

    public GridCell(VirtualNode<Link> virtualNode, VirtualNetwork<Link> virtualNetwork, Network network, //
            NetworkDistanceFunction minDist, NetworkDistanceFunction minTime, QuadTree<Link> linkTree) {
        this.myVNode = virtualNode;
        computeMaps(virtualNetwork, linkTree, minTime, minDist);
    }

    private void computeMaps(VirtualNetwork<Link> virtualNetwork, QuadTree<Link> links, //
            NetworkDistanceFunction minTime, NetworkDistanceFunction minDist) {
        for (VirtualNode<Link> toNode : virtualNetwork.getVirtualNodes()) {
            Link fromLink = links.getClosest(//
                    myVNode.getCoord().Get(0).number().doubleValue(), //
                    myVNode.getCoord().Get(1).number().doubleValue());
            Link toLink = links.getClosest(//
                    toNode.getCoord().Get(0).number().doubleValue(), //
                    toNode.getCoord().Get(1).number().doubleValue());
            double time = minTime.getTravelTime(fromLink, toLink);
            double distance = minDist.getDistance(fromLink, toLink);
            temporalSortedMap.put(time, toNode);
            temporalLookupMap.put(toNode, time);
            distanceSortedMap.put(distance, toNode);
        }
    }

    public List<VirtualNode<Link>> getDistNClosest(int n) {
        return GetSortedClosest.elem(n, distanceSortedMap);
    }
    
    public VirtualNode<Link> getDistAt(int n) {
        GlobalAssert.that(n>=0);
        int i =0;
        VirtualNode<Link> vNodeN = null;
        for(VirtualNode<Link> vNode : distanceSortedMap.values()){
            vNodeN = vNode;
            ++i;
            if(i==n)
                break;                        
        }
        return vNodeN;
    }

    public List<VirtualNode<Link>> getTimeNClosest(int n) {
        return GetSortedClosest.elem(n, temporalSortedMap);
    }

    public Double timeTo(VirtualNode<Link> virtualNode) {
        return temporalLookupMap.get(virtualNode);
    }
}
