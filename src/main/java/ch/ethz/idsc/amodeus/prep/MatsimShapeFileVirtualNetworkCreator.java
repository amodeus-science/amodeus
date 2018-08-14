/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;

import ch.ethz.idsc.amodeus.dispatcher.util.NetworkBounds;
import ch.ethz.idsc.amodeus.dispatcher.util.TensorLocation;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.MultiPolygons;
import ch.ethz.idsc.amodeus.virtualnetwork.MultiPolygonsVirtualNetworkCreator;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.tensor.Tensor;

public enum MatsimShapeFileVirtualNetworkCreator {
    ;

    public static VirtualNetwork<Link> creatVirtualNetwork(Network network, ScenarioOptions scenarioOptions) {
        File shapeFile = scenarioOptions.getShapeFile();
        boolean completeGraph = scenarioOptions.isCompleteGraph();
        GlobalAssert.that(shapeFile.exists());
        MultiPolygons multiPolygons = null;
        try {
            multiPolygons = new MultiPolygons(shapeFile);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Not able to load shapeFile for virtual Network creation. Stopping execution.");
            GlobalAssert.that(false);
        }

        @SuppressWarnings("unchecked")
        Collection<Link> elements = (Collection<Link>) network.getLinks().values();
        Tensor lbounds = NetworkBounds.lowerBoundsOf(network);
        Tensor ubounds = NetworkBounds.upperBoundsOf(network);

        Map<Node, HashSet<Link>> uElements = new HashMap<>();
        network.getNodes().values().forEach(n -> uElements.put(n, new HashSet<>()));
        network.getLinks().values().forEach(l -> uElements.get(l.getFromNode()).add(l));
        network.getLinks().values().forEach(l -> uElements.get(l.getToNode()).add(l));

        MultiPolygonsVirtualNetworkCreator<Link, Node> mpvnc = new MultiPolygonsVirtualNetworkCreator<>(multiPolygons, //
                elements, TensorLocation::of, NetworkCreatorUtils::linkToID, //
                uElements, lbounds, ubounds, completeGraph);

        return mpvnc.getVirtualNetwork();

    }

}
