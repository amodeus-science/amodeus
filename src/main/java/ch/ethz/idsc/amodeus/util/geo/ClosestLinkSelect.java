/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.geo;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;

/** Used to find the index of the closest {@link Network} {@link Link} to
 * a given coordinate in WGS84 format. The function first transforms the
 * coordinate in WGS84 coordinates to the local coordinate system (in m)
 * and then searches the closest link in the network to the coordinate. */
public class ClosestLinkSelect {

    private final MatsimAmodeusDatabase db;
    private final QuadTree<Link> qt;

    public ClosestLinkSelect(MatsimAmodeusDatabase db, QuadTree<Link> qt) {
        this.db = db;
        this.qt = qt;
    }

    public int indexFromWGS84(Coord wgs84location) {
        Coord local = db.referenceFrame.coords_fromWGS84().transform(wgs84location);
        return db.getLinkIndex(qt.getClosest(local.getX(), local.getY()));
    }
    
    
    public Link linkFromWGS84(Coord wgs84location){
        Coord local = db.referenceFrame.coords_fromWGS84().transform(wgs84location);
        return qt.getClosest(local.getX(), local.getY());
    }
}
