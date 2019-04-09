/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork;

import java.util.ArrayList;
import java.util.Collection;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import junit.framework.TestCase;

public class TrivialVirtualNetworkCreatorTest extends TestCase {
    public void testSimple() {
        Collection<Coord> collection = new ArrayList<>();
        collection.add(new Coord(1, 0));
        collection.add(new Coord(1, 1));
        collection.add(new Coord(0, 1));
        collection.add(new Coord(0, 0));

        Tensor meanOf = TrivialVirtualNetworkCreator.meanOf(collection, TrivialVirtualNetworkCreatorTest::ofCoord);
        assertEquals(meanOf, Tensors.vector(0.5, 0.5));
    }

    public static Tensor ofCoord(Coord c) {
        return Tensors.vector(c.getX(), c.getY());
    }
}
