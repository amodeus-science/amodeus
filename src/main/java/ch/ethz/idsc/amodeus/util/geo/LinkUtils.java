package ch.ethz.idsc.amodeus.util.geo;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;

public enum LinkUtils {
    ;

    public static int getLinkfromCoord(Coord gps, MatsimAmodeusDatabase db, QuadTree<Link> qt) {
        int linkIndex;
        Coord xy = db.referenceFrame.coords_fromWGS84().transform(gps);
        linkIndex = db.getLinkIndex(qt.getClosest(xy.getX(), xy.getY()));
        return linkIndex;
    }
}
