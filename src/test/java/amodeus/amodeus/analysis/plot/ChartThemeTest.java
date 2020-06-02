/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.plot;

import junit.framework.TestCase;

public class ChartThemeTest extends TestCase {
    public void testSimple() {
        assertEquals(ChartTheme.STANDARD, ChartTheme.valueOf("STANDARD"));
        assertEquals(ChartTheme.SHADOWS, ChartTheme.valueOf("SHADOWS"));
    }

    public void testFails() {
        try {
            ChartTheme.valueOf("FAIL");
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }
}
