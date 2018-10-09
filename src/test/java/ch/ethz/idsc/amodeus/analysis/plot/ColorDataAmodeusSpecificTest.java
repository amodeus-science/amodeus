/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.awt.Color;

import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import junit.framework.TestCase;

public class ColorDataAmodeusSpecificTest extends TestCase {
    public void testSimple() {
        for (ColorDataAmodeusSpecific cdas : ColorDataAmodeusSpecific.values()) {
            ColorDataIndexed colorDataIndexed = cdas.cyclic();
            Color color = colorDataIndexed.getColor(0);
            assertNotNull(color);
            assertNotNull(colorDataIndexed.getColor(1));
        }
    }
}
