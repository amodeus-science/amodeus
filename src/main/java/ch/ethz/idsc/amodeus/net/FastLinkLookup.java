/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree;

public class FastLinkLookup {
    private final MatsimStaticDatabase db;
    private final QuadTree<Link> quadTree;

    public FastLinkLookup(Network network, MatsimStaticDatabase db) {
        this.db = db;
        final double[] networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        quadTree = new QuadTree<>( //
                networkBounds[0], networkBounds[1], networkBounds[2], networkBounds[3]);
        for (Link link : db.getLinkInteger().keySet())
            quadTree.put(link.getCoord().getX(), link.getCoord().getY(), link);
    }

    public int getLinkFromWGS84(Coord gps) {
        return getLinkFromXY(db.referenceFrame.coords_fromWGS84().transform(gps));
    }

    public int getLinkFromXY(Coord xy) {
        return db.getLinkIndex(quadTree.getClosest(xy.getX(), xy.getY()));
    }

}
