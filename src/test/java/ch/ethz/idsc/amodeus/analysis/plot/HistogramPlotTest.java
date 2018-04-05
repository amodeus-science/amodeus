/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import junit.framework.TestCase;

public class HistogramPlotTest extends TestCase {

    public void test() {
        assertTrue(HistogramPlot.isInteger(5.5) == false);
        assertTrue(HistogramPlot.isInteger(7.0) == true);
        assertTrue(HistogramPlot.isInteger(Double.NaN) == false);
    }

}
