/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.data;

import amodeus.amodeus.testutils.TestLocationSpecs;
import ch.ethz.idsc.tensor.qty.Unit;
import junit.framework.TestCase;

public class LocationSpecDatabaseTest extends TestCase {
    public void testLookUp() {
        LocationSpecDatabase.INSTANCE.flush(); // necessary due to inconsistent test order

        assertTrue(LocationSpecDatabase.INSTANCE.isEmpty()); // needs to be in same test as travis ignores ordering

        LocationSpecDatabase.INSTANCE.put(TestLocationSpecs.SANFRANCISCO);

        assertFalse(LocationSpecDatabase.INSTANCE.isEmpty());

        try {
            LocationSpecDatabase.INSTANCE.fromString("OTHER");
            fail();
        } catch (NullPointerException e) {
            assertTrue(true);
        }

        assertEquals(Unit.of("ft"), LocationSpecDatabase.INSTANCE.fromString("SANFRANCISCO").referenceFrame().unit());
    }
}
