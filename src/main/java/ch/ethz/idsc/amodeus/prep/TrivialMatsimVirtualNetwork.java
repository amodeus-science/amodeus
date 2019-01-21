/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;

import ch.ethz.idsc.amodeus.dispatcher.util.TensorLocation;
import ch.ethz.idsc.amodeus.virtualnetwork.TrivialVirtualNetworkCreator;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;

/* package */ enum TrivialMatsimVirtualNetwork {
    ;

    /** @param network
     * @return trivial {@link VirtualNetwork} in which all {@link Link} in the
     *         {@link Network} belong to one central {@link VirtualNode} */
    public static VirtualNetwork<Link> createVirtualNetwork(Network network) {
        @SuppressWarnings("unchecked")
        Collection<Link> elements = (Collection<Link>) network.getLinks().values();

        Map<Node, HashSet<Link>> uElements = new HashMap<>();
        network.getNodes().values().forEach(n -> uElements.put(n, new HashSet<>()));
        network.getLinks().values().forEach(l -> uElements.get(l.getFromNode()).add(l));
        network.getLinks().values().forEach(l -> uElements.get(l.getToNode()).add(l));

        TrivialVirtualNetworkCreator<Link, Node> tvnc = new TrivialVirtualNetworkCreator<>(//
                elements, TensorLocation::of, NetworkCreatorUtils::linkToID, uElements, false);
        return tvnc.getVirtualNetwork();
    }
}