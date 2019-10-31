/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.data;

import ch.ethz.idsc.amodeus.testutils.TestLocationSpecs;
import ch.ethz.idsc.tensor.qty.Unit;
import junit.framework.TestCase;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LocationSpecDatabaseTest extends TestCase {
    public void testEmpty() {
        assertTrue(LocationSpecDatabase.INSTANCE.isEmpty());
    }

    public void testLookUp() {
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
