/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.plot;

import org.jfree.chart.JFreeChart;

/** similar to {@link Histogram} but with bars stacked on top of each other
 * instead of next to each other */
public enum StackedHistogram {
    ;

    public static JFreeChart of(VisualSet visualSet) {
        return Histogram.of(visualSet, true);
    }

}
