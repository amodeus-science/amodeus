/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;

public enum NodeAdjacencyMap {
    ;

    /** @return a {@link Map} in which every {@link Node} is associated to
     *         a list of unique {@link Link}s which are adjacent to the node
     *         in the {@link Network} @param network */
    public static Map<Node, Set<Link>> of(Network network) {
        Map<Node, Set<Link>> uElements = new HashMap<>();
        // create empty hash sets for every node
        network.getNodes().values().forEach(n -> uElements.put(n, new HashSet<>()));
        // add all links to the sets of their from/to nodes
        network.getLinks().values().forEach(l -> {
            uElements.get(l.getFromNode()).add(l);
            uElements.get(l.getToNode()).add(l);
        });
        return uElements;
    }

}
