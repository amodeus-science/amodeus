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

    public static VirtualNetwork<Link> createVirtualNetwork(Population population, Network network, int numVNodes, boolean completeGraph) {
        // make sure numVNodes is power of 2
        for (int i = numVNodes; i > 1;) {
            GlobalAssert.that(i % 2 == 0);
            i /= 2;
        }

        Set<Request> requests = PopulationUtils.getAVRequests(population, network);

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
