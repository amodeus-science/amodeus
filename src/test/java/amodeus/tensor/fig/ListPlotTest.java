/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.tensor.fig;

import junit.framework.TestCase;

public class ListPlotTest extends TestCase {
    public void testSimple() {
        VisualSet visualSet = new VisualSet();
        ListPlot.of(visualSet);
    }
}
