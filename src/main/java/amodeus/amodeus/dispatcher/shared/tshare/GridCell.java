/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.tshare;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.collections.QuadTree;

import amodeus.amodeus.routing.CachedNetworkTimeDistance;
import amodeus.amodeus.routing.NetworkTimeDistInterface;
import amodeus.amodeus.util.math.GlobalAssert;
import amodeus.amodeus.util.math.SI;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;
import amodeus.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.sca.Sign;

/** In AMoDeus a {@link GridCell} is implemented as an augmented version of the {@link VirtualNode}
 * that is already used in other places to subdivide a road network into separate areas. Each
 * {@link GridCell} has a number of data structures to determine travel time and distance to
 * other {@link GridCell}s. */
/* package */ class GridCell {

    /** every {@link GridCell} corresponds do exactly one {@link VirtualNode} */
    private final VirtualNode<Link> virtualNode;

    /** {@link NavigableMap} of other {@link GridCell}s ({@link VirtualNode}s) according to freeflow travel time */
    private final NavigableMap<Scalar, VirtualNode<Link>> temporalSortedMap = new TreeMap<>();

    /** {@link NavigableMap} of other {@link GridCell}s ({@link VirtualNode}s) according to travel distance */
    private final NavigableMap<Scalar, VirtualNode<Link>> distanceSortedMap = new TreeMap<>();

    /** {@link NavigableMap} used to rapidly access the set of {@link GridCell}s ({@link VirtualNode}s) reachable
     * within a travel time of less than some {@link Scalar} value */
    private final NavigableMap<Scalar, List<VirtualNode<Link>>> nodesWithinLessThan = new TreeMap<>();

    /** Construction of a {@link GridCell} requires: a {@link VirtualNode} @param virtualNode in a
     * {@link VirtualNetwork} @param virtualNetwork that uniquely defines the {@link GridCell},
     * functions to compute the minimum travel time and distance to other {@link VirtualNode}s,
     * which are @param minDist and @param minTime, respectively. A {@link QuadTree} @param linkTree to compute
     * the nearest {@link Link}s */
    public GridCell(VirtualNode<Link> virtualNode, VirtualNetwork<Link> virtualNetwork, //
            CachedNetworkTimeDistance minDist, NetworkTimeDistInterface minTime, QuadTree<Link> linkTree) {
        this.virtualNode = virtualNode;
        computeMaps(virtualNetwork, linkTree, minDist, minTime);
    }

    private void computeMaps(VirtualNetwork<Link> virtualNetwork, QuadTree<Link> links, //
            CachedNetworkTimeDistance minDist, NetworkTimeDistInterface minTime) {
        /** the from link is the link closes to the center of the {@link VirtualNode} */
        Link gridCellCenterLink = links.getClosest(//
                virtualNode.getCoord().Get(0).number().doubleValue(), //
                virtualNode.getCoord().Get(1).number().doubleValue());
        /** calculate distances and travel times to other nodes */
        for (VirtualNode<Link> otherGridCell : virtualNetwork.getVirtualNodes()) {
            Link otherCenterLink = links.getClosest(//
                    otherGridCell.getCoord().Get(0).number().doubleValue(), //
                    otherGridCell.getCoord().Get(1).number().doubleValue());
            Scalar time = minTime.travelTime(gridCellCenterLink, otherCenterLink, 0.0);
            Scalar distance = minDist.distance(gridCellCenterLink, otherCenterLink, 0.0);
            temporalSortedMap.put(time, otherGridCell);
            distanceSortedMap.put(distance, otherGridCell);
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

    /** @return the {@link VirtualNode} at the @param position from the
     *         list of other {@link VirtualNode}s sorted according to the distance. */
    public VirtualNode<Link> getVNodeAt(int position) {
        GlobalAssert.that(position >= 0);
        int i = 0;
        for (Entry<Scalar, VirtualNode<Link>> entry : distanceSortedMap.entrySet()) {
            if (i == position)
                return entry.getValue();
            ++i;
        }
        return distanceSortedMap.lastEntry().getValue();
    }

    /** @return {@link List} of all {@link VirtualNode}s reachable within
     *         the {@link Scalar} @param time */
    public List<VirtualNode<Link>> nodesReachableWithin(Scalar time) {
        return nodesWithinLessThan.lowerEntry(Sign.requirePositiveOrZero(time)).getValue();
    }

}
