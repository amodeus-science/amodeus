/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;
import junit.framework.TestCase;

public class RemoveUnitTest extends TestCase {

    public void testQuantity() {
        Scalar scalar = RemoveUnit.FUNCTION.apply(Quantity.of(3, "s"));
        assertEquals(scalar, RealScalar.of(3));
    }

    public void testScalar() {
        Scalar scalar = RemoveUnit.FUNCTION.apply(RealScalar.of(-3));
        assertEquals(scalar, RealScalar.of(-3));
    }

}
