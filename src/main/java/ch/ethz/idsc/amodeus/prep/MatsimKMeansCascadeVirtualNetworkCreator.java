/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.dispatcher.util.TensorLocation;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.KMeansCascadeVirtualNetworkCreator;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;

public enum MatsimKMeansCascadeVirtualNetworkCreator {
    ;

    /** The network is split into two halfs and then each half is recursively split again until there are numVNodes parts. This sort of partitioning is useful when
     * a cascade of the virtual network is needed. It approximates a normal KMeans algorithm in a faster way.
     * 
     * @param population
     * @param network
     * @param numVNodes required to be a number of two potency
     * @param completeGraph
     * @return {@link VirtualNetwork} with numVNodes many nodes */
    public static VirtualNetwork<Link> createVirtualNetwork(Population population, Network network, int numVNodes, boolean completeGraph) {
        // make sure numVNodes is power of 2
        for (int i = numVNodes; i > 1;) {
            GlobalAssert.that(i % 2 == 0);
            i /= 2;
        }

        Set<Request> requests = PopulationTools.getAVRequests(population, network);

        @SuppressWarnings("unchecked")
        Collection<Link> elements = (Collection<Link>) network.getLinks().values();

        Map<Node, HashSet<Link>> uElements = new HashMap<>();

        network.getNodes().values().forEach(n -> uElements.put(n, new HashSet<>()));

        network.getLinks().values().forEach(l -> uElements.get(l.getFromNode()).add(l));
        network.getLinks().values().forEach(l -> uElements.get(l.getToNode()).add(l));

        int tryIterations = 100;
        KMeansCascadeVirtualNetworkCreator vnc = new KMeansCascadeVirtualNetworkCreator( //
                requests, elements, uElements, TensorLocation::of, //
                NetworkCreatorUtils::linkToID, numVNodes, completeGraph, tryIterations);

        VirtualNetwork<Link> virtualNetwork = vnc.getVirtualNetwork();
        return virtualNetwork;

    }
}
