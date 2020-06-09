/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.net;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import junit.framework.TestCase;

public class TensorCoordsTest extends TestCase {
    public void testSimple() {
        Coord c = TensorCoords.toCoord(Tensors.vector(3, 8));
        assertEquals(c.getX(), 3.0);
        assertEquals(c.getY(), 8.0);
        Tensor v = TensorCoords.toTensor(c);
        assertEquals(v, Tensors.vector(3, 8));
    }
}
