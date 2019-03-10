/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.math;

import static org.junit.Assert.assertNotEquals;

import junit.framework.TestCase;

public class IntPointTest extends TestCase {

    public void testSimple() {
        assertEquals(new IntPoint(2, 3), new IntPoint(2, 3));
        assertNotEquals(new IntPoint(2, 3), new IntPoint(2, 4));
        assertNotEquals(new IntPoint(2, 3), new IntPoint(3, 3));
        assertEquals(new IntPoint(2, 3).hashCode(), new IntPoint(2, 3).hashCode());
        assertEquals(new IntPoint(12, 3).hashCode(), new IntPoint(12, 3).hashCode());
        assertNotEquals(new IntPoint(12, 3).hashCode(), new IntPoint(12, 13).hashCode());
    }

}
