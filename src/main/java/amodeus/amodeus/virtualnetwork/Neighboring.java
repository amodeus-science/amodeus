/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.virtualnetwork;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;

import amodeus.amodeus.util.math.IntPoint;
import amodeus.amodeus.virtualnetwork.core.GenericButterfliesAndRainbows;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;
import amodeus.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.red.Total;

public class Neighboring {

    /** neighbors[i,j] = 1 if i is neighboring to j , symmetric matrix */
    private final Tensor neighbors;

    public Neighboring(VirtualNetwork<Link> virtualNetwork, Network network) {
        neighbors = Array.zeros(virtualNetwork.getvNodesCount(), virtualNetwork.getvNodesCount());

        Map<Node, Set<Link>> uElements = network.getNodes().values().stream().collect(Collectors.toMap(n -> n, n -> new HashSet<>()));
        network.getLinks().values().stream().collect(Collectors.groupingBy(Link::getFromNode)).forEach((node, links) -> uElements.get(node).addAll(links));
        network.getLinks().values().stream().collect(Collectors.groupingBy(Link::getToNode)).forEach((node, links) -> uElements.get(node).addAll(links));

        GenericButterfliesAndRainbows<Link, Node> gbar = new GenericButterfliesAndRainbows<>();
        uElements.forEach((node, links) -> links.stream().map(virtualNetwork::getVirtualNode).forEach(vn -> gbar.add(node, vn)));

        Collection<IntPoint> collection = gbar.allPairs();

        System.out.println("collection.size " + collection.size());

        for (IntPoint point : collection) {
            System.out.println("point:  " + point);
            VirtualNode<Link> vNfrom = virtualNetwork.getVirtualNode(point.x);
            VirtualNode<Link> vNto = virtualNetwork.getVirtualNode(point.y);
            neighbors.set(RealScalar.ONE, vNfrom.getIndex(), vNto.getIndex());
        }
    }

    public boolean check(VirtualNode<Link> vN1, VirtualNode<Link> vN2) {
        return neighbors.Get(vN1.getIndex(), vN2.getIndex()).equals(RealScalar.ONE);
    }

    public int geNumNeighbors(VirtualNode<Link> vN) {
        Scalar num = (Scalar) Total.of(neighbors.get(vN.getIndex()));
        return num.number().intValue();
    }
}
