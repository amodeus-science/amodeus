/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.lp;

import ch.ethz.idsc.amodeus.util.math.Magnitude;
import junit.framework.TestCase;

public class LPUtilsTest extends TestCase {
    public void testSimple() {
        double double1 = Magnitude.VELOCITY.toDouble(LPUtils.AVERAGE_VEL);
        assertEquals(double1, 8.333333333333334, 1e-8);
    }
}
