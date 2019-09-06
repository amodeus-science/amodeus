/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.dispatcher.util.NetworkBounds;
import ch.ethz.idsc.amodeus.dispatcher.util.TensorLocation;
import ch.ethz.idsc.amodeus.util.network.NodeAdjacencyMap;
import ch.ethz.idsc.amodeus.virtualnetwork.KMeansVirtualNetworkCreator;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
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

        Map<Node, HashSet<Link>> uElements = NodeAdjacencyMap.of(network);

        int tryIterations = 100;
        KMeansVirtualNetworkCreator<Link, Node> vnc = new KMeansVirtualNetworkCreator<>( //
                data, elements, uElements, TensorLocation::of, //
                NetworkCreatorUtils::linkToID, lbounds, ubounds, numVNodes, completeGraph, tryIterations);

        return vnc.getVirtualNetwork();

    }
}
