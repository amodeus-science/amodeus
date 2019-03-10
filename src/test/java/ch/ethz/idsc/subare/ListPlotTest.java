/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare;

import ch.ethz.idsc.subare.plot.ListPlot;
import ch.ethz.idsc.subare.plot.VisualSet;
import junit.framework.TestCase;

public class ListPlotTest extends TestCase {
    public void testSimple() {
        VisualSet visualSet = new VisualSet();
        ListPlot.of(visualSet);
    }
}
