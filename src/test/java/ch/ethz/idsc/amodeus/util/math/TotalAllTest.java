/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.math;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.mat.DiagonalMatrix;
import junit.framework.TestCase;

public class TotalAllTest extends TestCase {
    public void testSimple() {
        Scalar sum = TotalAll.of(DiagonalMatrix.of(1, 2, 3));
        assertEquals(sum, RealScalar.of(6));
    }

    public void testFail() {
        try {
            TotalAll.of(RealScalar.of(6));
            assertTrue(false);
        } catch (Exception exception) {
            // ---
        }
    }
}
