/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.gfx;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensors;
import junit.framework.TestCase;

public class StaticHelperTest extends TestCase {
    public void testEmpty() {
        assertEquals(StaticHelper.meanOrZero(Tensors.empty()), RealScalar.ZERO);
    }

    public void testSome() {
        assertEquals(StaticHelper.meanOrZero(Tensors.vector(1)), RealScalar.ONE);
    }
}
