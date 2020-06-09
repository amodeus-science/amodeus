/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.net;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.collections.QuadTree;

import amodeus.amodeus.util.geo.FastQuadTree;
import ch.ethz.idsc.tensor.Tensor;

public class FastLinkLookup {
    private final MatsimAmodeusDatabase db;
    private final QuadTree<Link> quadTree;

    public FastLinkLookup(Network network, MatsimAmodeusDatabase db) {
        this(FastQuadTree.of(network), db);
    }

    public FastLinkLookup(QuadTree<Link> quadTree, MatsimAmodeusDatabase db) {
        this.quadTree = quadTree;
        this.db = db;
    }

    // index getters

    /** Used to find the index of the closest {@link Network} {@link Link} to
     * a given coordinate in WGS84 format. The function first transforms the
     * coordinate in WGS84 coordinates to the local coordinate system (in m)
     * and then searches the closest link in the network to the coordinate. */
    public int indexFromWGS84(Tensor wgs84location) {
        return indexFromLocal(TensorCoords.toCoord(wgs84location));
    }

    public int indexFromWGS84(Coord wgs84location) {
        return linkFromWGS84(wgs84location).getId().index();
    }

    public int indexFromLocal(Tensor local) {
        return indexFromLocal(TensorCoords.toCoord(local));
    }

    public int indexFromLocal(Coord local) {
        return linkFromLocal(local).getId().index();
    }

    // link getters

    public Link linkFromWGS84(Tensor wgs84location) {
        return linkFromWGS84(TensorCoords.toCoord(wgs84location));
    }

    public Link linkFromWGS84(Coord wgs84location) {
        Coord local = db.referenceFrame.coords_fromWGS84().transform(wgs84location);
        return linkFromLocal(local);
    }

    public Link linkFromLocal(Tensor local) {
        return linkFromLocal(TensorCoords.toCoord(local));
    }

    public Link linkFromLocal(Coord local) {
        return quadTree.getClosest(local.getX(), local.getY());
    }

    // coordinate getters

    public Tensor wgs84fromLink(Link link) {
        Coord local = link.getCoord();
        return TensorCoords.toTensor(db.referenceFrame.coords_toWGS84().transform(local));
    }

    public Tensor localFromLink(Link link) {
        return TensorCoords.toTensor(link.getCoord());
    }
}
