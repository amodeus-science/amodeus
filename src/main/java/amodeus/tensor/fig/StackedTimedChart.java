/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.tensor.fig;

import org.jfree.chart.JFreeChart;

public enum StackedTimedChart {
    ;

    public static JFreeChart of(VisualSet visualSet) {
        return TimedChart.of(visualSet, true);
    }

}
