/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.plot;

import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.ColorDataLists;
import junit.framework.TestCase;

public class ColorDataAmodeusTest extends TestCase {
    public void testSimple() {
        ColorDataIndexed colorDataIndexed = ColorDataAmodeus.indexed("097");
        assertEquals(colorDataIndexed.getColor(2), ColorDataLists._097.cyclic().getColor(2));
    }
}
