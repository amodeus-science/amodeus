/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.tensor.fig;

import junit.framework.TestCase;

public class HistogramTest extends TestCase {
    public void testEmpty() {
        VisualSet visualSet = new VisualSet();
        Histogram.of(visualSet);
    }
}
