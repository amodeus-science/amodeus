/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.view.gheat.gui;

import junit.framework.TestCase;

public class ColorSchemesTest extends TestCase {
    public void testSimple() {
        for (ColorSchemes colorSchemes : ColorSchemes.values()) {
            colorSchemes.colorDataIndexed.getColor(0);
            colorSchemes.colorDataIndexed.getColor(255);
            try {
                colorSchemes.colorDataIndexed.getColor(256);
                fail();
            } catch (Exception exception) {
                // ---
            }
        }
    }
}
