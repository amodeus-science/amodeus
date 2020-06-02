/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.view.gheat.gui;

import java.util.Objects;

import junit.framework.TestCase;

public class GheatPalettesTest extends TestCase {
    public void testSimple() {
        for (GheatPalettes gheatPalettes : GheatPalettes.values()) {
            Objects.requireNonNull(gheatPalettes.colorDataIndexed);
            assertEquals(gheatPalettes.colorDataIndexed.length(), 256);
        }
    }
}
