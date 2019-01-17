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
import ch.ethz.idsc.amodeus.virtualnetwork.RectangleGridVirtualNetworkCreator;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class MatsimRectangleVirtualNetworkCreator {

    public static VirtualNetwork<Link> createVirtualNetwork(Population population, Network network, boolean completeGraph, //
            int divLat, int divLng) {
        @SuppressWarnings("unchecked")

        /** bounds */
        Tensor bounds = NetworkBounds.of(network);
        Tensor lbounds = bounds.get(0);
        Tensor ubounds = bounds.get(1);
        Tensor xBounds = Tensors.of(lbounds.Get(0), ubounds.Get(0));
        Tensor yBounds = Tensors.of(lbounds.Get(1), ubounds.Get(1));
        System.out.println("Network bounds:  " + xBounds + " , " + yBounds);

        /** u elements to determine neighbors */
        Map<Node, HashSet<Link>> uElements = new HashMap<>();
        network.getNodes().values().forEach(n -> uElements.put(n, new HashSet<>()));
        network.getLinks().values().forEach(l -> uElements.get(l.getFromNode()).add(l));
        network.getLinks().values().forEach(l -> uElements.get(l.getToNode()).add(l));

        Collection<Link> elements = (Collection<Link>) network.getLinks().values();
        RectangleGridVirtualNetworkCreator<Link, Node> creator = //
                new RectangleGridVirtualNetworkCreator<>(elements, TensorLocation::of, NetworkCreatorUtils::linkToID, //
                        divLat, divLng, xBounds, yBounds, //
                        uElements, completeGraph);
        return creator.getVirtualNetwork();
    }
}
