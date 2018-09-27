package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/** Maintains a list of {@link AVRequest}s in a {@link Set} and a
 * {@link QuadTree} to enable both fast "contains" searches and
 * "closest" searches
 * 
 * @author clruch */
public class RequestTreeMaintainer {

    private final QuadTree<AVRequest> tree;
    /** data structures are used to enable fast "contains" searching */
    private final Set<AVRequest> set = new HashSet<>();
    private final Function<AVRequest, Coord> location;

    public RequestTreeMaintainer(Network network, Function<AVRequest, Coord> location) {
        this.location = location;
        double[] networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        tree = new QuadTree<>(networkBounds[0], networkBounds[1], networkBounds[2], networkBounds[3]);
    }

    /** @return closest {@link AVRequest} in tree from {@link Coord} @param coord */
    public AVRequest getClosest(Coord coord) {
        return tree.getClosest(coord.getX(), coord.getY());
    }

    /** Adds the {@link AVRequest} @param request to the Tree Maintainer. */
    public void add(AVRequest request) {
        if (!set.contains(request)) {
            Coord coord = location.apply(request);
            boolean setok = set.add(request);
            boolean treeok = tree.put(coord.getX(), coord.getY(), request);
            GlobalAssert.that(setok && treeok);
        }
    }

    /** Removes the {@link AVRequest} @param request from the Tree Maintainer. */
    public void remove(AVRequest request) {
        Coord coord = location.apply(request);
        boolean setok = set.remove(request);
        boolean treeok = tree.remove(coord.getX(), coord.getY(), request);
        GlobalAssert.that(setok && treeok);
    }
}
