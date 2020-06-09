/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.math;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensors;
import junit.framework.TestCase;

public class MeanOrZeroTest extends TestCase {
    public void testEmpty() {
        assertEquals(MeanOrZero.of(Tensors.empty()), RealScalar.ZERO);
    }

    public void testVector() {
        assertEquals(MeanOrZero.of(Tensors.vector(0, 2)), RealScalar.ONE);
    }
}
