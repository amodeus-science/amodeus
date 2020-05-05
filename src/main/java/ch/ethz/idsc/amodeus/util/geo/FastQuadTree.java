/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.geo;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree;

public enum FastQuadTree {
    ;

    /** @param network {@link Network}
     * @return {@link QuadTree} with {@link Link}s in {@link Network} */
    public static QuadTree<Link> of(Network network) {
        double[] networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        QuadTree<Link> quadTree = new QuadTree<>( //
                networkBounds[0], networkBounds[1], networkBounds[2], networkBounds[3]);
        for (Link link : network.getLinks().values())
            quadTree.put(link.getCoord().getX(), link.getCoord().getY(), link);
        return quadTree;
    }
}
