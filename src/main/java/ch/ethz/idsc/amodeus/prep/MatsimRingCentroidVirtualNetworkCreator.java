/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.idsc.amodeus.dispatcher.util.TensorLocation;
import ch.ethz.idsc.amodeus.util.math.CreateQuadTree;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.network.NodeAdjacencyMap;
import ch.ethz.idsc.amodeus.virtualnetwork.CentroidVirtualNetworkCreator;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;

/** This is a demo of functionality for the centroid virtual network creator, centroid locations
 * are placed on a half ring structure... */
public class MatsimRingCentroidVirtualNetworkCreator {

    public static VirtualNetwork<Link> createVirtualNetwork(Population population, Network network, int numVNodes, //
            boolean completeGraph) {
        @SuppressWarnings("unchecked")
        Collection<Link> elements = (Collection<Link>) network.getLinks().values();
        Map<Node, Set<Link>> uElements = NodeAdjacencyMap.of(network);

        /** generating centroids on a ring */
        List<Link> centroids = getRingCentroids(network, numVNodes);

        /** create the virtual network using the centroidvirtualNetworkCreator */
        CentroidVirtualNetworkCreator<Link, Node> vnc = new CentroidVirtualNetworkCreator<>(//
                elements, centroids, TensorLocation::of, NetworkCreatorUtils::linkToID, uElements, completeGraph);
        return vnc.getVirtualNetwork();
    }

    private static List<Link> getRingCentroids(Network network, int numVNodes) {
        List<Link> centroids = new ArrayList<>();
        double[] bounds = NetworkUtils.getBoundingBox(network.getNodes().values()); // minX, minY, maxX, maxY
        QuadTree<Link> qt = CreateQuadTree.of(network);

        // center location
        double centerX = bounds[0] + 0.5 * (bounds[2] - bounds[0]);
        double centerY = bounds[1] + 0.5 * (bounds[3] - bounds[1]);

        double radius = 0.5 * Math.min(bounds[2] - bounds[0], bounds[3] - bounds[1]);
        centroids.add(qt.getClosest(centerX, centerY));

        for (int count = 1; count < numVNodes; ++count) {
            double arg = count / (numVNodes - 1.0) * 2 * Math.PI;
            double posX = centerX + radius * Math.cos(arg);
            double posY = centerY + radius * Math.sin(arg);
            Link closest = qt.getClosest(posX, posY);
            centroids.add(closest);
        }
        GlobalAssert.that(centroids.size() == numVNodes);
        return centroids;
    }
}
