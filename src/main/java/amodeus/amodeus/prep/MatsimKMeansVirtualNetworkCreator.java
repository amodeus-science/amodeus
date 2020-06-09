/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.prep;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Population;

import amodeus.amodeus.dispatcher.util.NetworkBounds;
import amodeus.amodeus.dispatcher.util.TensorLocation;
import amodeus.amodeus.util.network.NodeAdjacencyMap;
import amodeus.amodeus.virtualnetwork.KMeansVirtualNetworkCreator;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.tensor.Tensor;

public enum MatsimKMeansVirtualNetworkCreator {
    ;

    public static VirtualNetwork<Link> createVirtualNetwork(Population population, Network network, int numVNodes, boolean completeGraph) {
        double data[][] = NetworkCreatorUtils.fromPopulation(population, network);
        @SuppressWarnings("unchecked")
        Collection<Link> elements = (Collection<Link>) network.getLinks().values();
        Tensor bounds = NetworkBounds.of(network);
        Tensor lbounds = bounds.get(0);
        Tensor ubounds = bounds.get(1);

        Map<Node, Set<Link>> uElements = NodeAdjacencyMap.of(network);

        int tryIterations = 100;
        KMeansVirtualNetworkCreator<Link, Node> vnc = new KMeansVirtualNetworkCreator<>( //
                data, elements, uElements, TensorLocation::of, //
                NetworkCreatorUtils::linkToID, lbounds, ubounds, numVNodes, completeGraph, tryIterations);

        return vnc.getVirtualNetwork();

    }
}
