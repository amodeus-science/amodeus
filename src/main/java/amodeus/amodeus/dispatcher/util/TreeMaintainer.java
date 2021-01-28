/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.collections.QuadTree.Rect;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.VectorQ;

/** Maintains a list of {@link T}s in a {@link Set} and a
 * {@link QuadTree} to enable both fast "contains" searches and
 * "closest" searches, T might be for instance {@link PassengerRequest}s
 * or {@link RoboTaxi}s
 * 
 * @author clruch */
public class TreeMaintainer<T> {

    private final QuadTree<T> quadTree;
    /** data structures are used to enable fast "contains" searching */
    private final Set<T> set = new HashSet<>();
    private final Function<T, Tensor> location;

    /** For the Checks */
    private final Rect outerRect;

    /** @param networkBounds is in format (minX, minY, maxX, maxY)
     * @param location */
    public TreeMaintainer(double[] networkBounds, Function<T, Tensor> location) {
        this.location = location;
        this.outerRect = new Rect(networkBounds[0], networkBounds[1], networkBounds[2], networkBounds[3]);
        quadTree = new QuadTree<>(networkBounds[0], networkBounds[1], networkBounds[2], networkBounds[3]);
    }

    /** @return closest {@link T} in tree from {@link Tensor} @param coord */
    public T getClosest(Tensor coord) {
        VectorQ.requireLength(coord, 2); // ensure that vector of length 2;
        return quadTree.getClosest( //
                coord.Get(0).number().doubleValue(), //
                coord.Get(1).number().doubleValue());
    }

    /** Adds the {@link T} @param t to the Tree Maintainer if it is not yet contained in the tree. */
    public void add(T t) {
        if (!set.contains(t)) {
            Tensor coord = location.apply(t);
            boolean setok = set.add(t);
            boolean treeok = quadTree.put( //
                    coord.Get(0).number().doubleValue(), //
                    coord.Get(1).number().doubleValue(), t);
            GlobalAssert.that(setok && treeok);
        }
    }

    /** Removes the {@link T} @param t from the Tree Maintainer. */
    public void remove(T t) {
        Tensor coord = location.apply(t);
        boolean setok = set.remove(t);
        boolean treeok = quadTree.remove( //
                coord.Get(0).number().doubleValue(), //
                coord.Get(1).number().doubleValue(), t);
        GlobalAssert.that(setok && treeok);
    }

    public boolean contains(T t) {
        return set.contains(t);
    }

    public int size() {
        GlobalAssert.that(quadTree.size() == set.size());
        return quadTree.size();
    }

    public Collection<T> inFrame(Rect bounds) {
        return quadTree.getRectangle(bounds, new HashSet<>());
    }

    /** Gets all objects within a certain distance around x/y
     *
     * @param x left-right location, longitude
     * @param y up-down location, latitude
     * @param distance the maximal distance returned objects can be away from x/y
     * @return the objects found within distance to x/y */
    public Collection<T> disk(double x, double y, double distance) {
        return quadTree.getDisk(x, y, distance);
    }

    public boolean contains(Tensor coord) {
        VectorQ.requireLength(coord, 2); // ensure that vector of length 2;
        return outerRect.contains( //
                coord.Get(0).number().doubleValue(), //
                coord.Get(1).number().doubleValue());
    }

    public Set<T> getValues() {
        return set;
    }

    /** Clears the whole tree. After this method is called no elements will remain */
    public void clear() {
        quadTree.clear();
        set.clear();
    }
}
