package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree;

/* package */ enum FastQuadTree {
    ;

    /** @param network
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
