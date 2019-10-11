/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.idsc.amodeus.routing.CachedNetworkTimeDistance;
import ch.ethz.idsc.amodeus.routing.NetworkTimeDistInterface;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.sca.Sign;

/* package */ class GridCell {

    // private final VirtualNetwork<Link> virtualNetwork;
    private final VirtualNode<Link> myVNode;
    private final NavigableMap<Scalar, VirtualNode<Link>> temporalSortedMap = new TreeMap<>();
    private final NavigableMap<Scalar, VirtualNode<Link>> distanceSortedMap = new TreeMap<>();
    private final NavigableMap<Scalar, List<VirtualNode<Link>>> nodesWithinLessThan = new TreeMap<>();
    private final Map<VirtualNode<Link>, Scalar> temporalLookupMap = new HashMap<>();

    public GridCell(VirtualNode<Link> virtualNode, VirtualNetwork<Link> virtualNetwork, Network network, //
            CachedNetworkTimeDistance minDist, NetworkTimeDistInterface minTime, QuadTree<Link> linkTree, //
            double now) {
        this.myVNode = virtualNode;
        // this.virtualNetwork = virtualNetwork;
        computeMaps(virtualNetwork, linkTree, minDist, minTime, now);
    }

    private void computeMaps(VirtualNetwork<Link> virtualNetwork, QuadTree<Link> links, //
            CachedNetworkTimeDistance minDist, NetworkTimeDistInterface minTime, double now) {
        /** calculate distances and travel times to other nodes */
        for (VirtualNode<Link> toNode : virtualNetwork.getVirtualNodes()) {
            Link fromLink = links.getClosest(//
                    myVNode.getCoord().Get(0).number().doubleValue(), //
                    myVNode.getCoord().Get(1).number().doubleValue());
            Link toLink = links.getClosest(//
                    toNode.getCoord().Get(0).number().doubleValue(), //
                    toNode.getCoord().Get(1).number().doubleValue());
            Scalar time = minTime.travelTime(fromLink, toLink, now);
            Scalar distance = minDist.distance(fromLink, toLink, now);
            temporalSortedMap.put(time, toNode);
            temporalLookupMap.put(toNode, time);
            distanceSortedMap.put(distance, toNode);
        }

        /** fill map with n nodes less than time */
        nodesWithinLessThan.put(Quantity.of(-0.0000001, SI.SECOND), new ArrayList<>());
        for (Entry<Scalar, VirtualNode<Link>> entry : temporalSortedMap.entrySet()) {
            List<VirtualNode<Link>> updatedList = new ArrayList<>();
            nodesWithinLessThan.lastEntry().getValue().forEach(vn -> updatedList.add(vn));
            updatedList.add(entry.getValue());
            nodesWithinLessThan.put(entry.getKey(), updatedList);
        }

    }

    public List<VirtualNode<Link>> getDistNClosest(int n) {
        return GetSortedClosest.elem(n, distanceSortedMap);
    }

    public VirtualNode<Link> getDistAt(int n) {
        GlobalAssert.that(n >= 0);
        int i = 0;
        VirtualNode<Link> vNodeN = null;
        for (VirtualNode<Link> vNode : distanceSortedMap.values()) {
            vNodeN = vNode;
            ++i;
            if (i == n)
                break;
        }
        return vNodeN;
    }

    public List<VirtualNode<Link>> getTimeNClosest(int n) {
        return GetSortedClosest.elem(n, temporalSortedMap);
    }

    public List<VirtualNode<Link>> nodesReachableWithin(Scalar time) {
        return nodesWithinLessThan.lowerEntry(Sign.requirePositiveOrZero(time)).getValue();

        //// NavigableMap<Scalar,List<VirtualNode<Link>>> nodesWithinLessthan = new TreeMap<>();
        //// return nodesWithinLessthan(time);
        //
        // List<VirtualNode<Link>> closeEnough = new ArrayList<>();
        // int i = 1;
        // boolean withinLimit = true;
        // while (withinLimit && i < virtualNetwork.getvNodesCount()) {
        // closeEnough = getTimeNClosest(i);
        // if (Scalars.lessEquals(time, timeTo(closeEnough.get(i - 1)))) {
        // withinLimit = false;
        // }
        // ++i;
        // }
        // return closeEnough;
    }

    public Scalar timeTo(VirtualNode<Link> virtualNode) {
        return temporalLookupMap.get(virtualNode);
    }
}
