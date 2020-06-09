/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis;

import amodeus.amodeus.util.math.UnitStringScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;
import junit.framework.TestCase;

public class UnitStringScalarTest extends TestCase {
    public void testSimple() {
        {
            Scalar apply = UnitStringScalar.FUNCTION.apply(Quantity.of(3, "m*s"));
            assertEquals(apply.toString(), "\"m*s\"");
        }
        {
            Scalar apply = UnitStringScalar.FUNCTION.apply(RealScalar.of(3));
            assertEquals(apply.toString(), "\"\"");
        }
    }
}
