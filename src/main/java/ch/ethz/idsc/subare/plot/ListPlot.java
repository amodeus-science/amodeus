/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.plot;

import org.jfree.chart.JFreeChart;

public enum ListPlot {
    ;

    public static JFreeChart of(VisualSet visualSet) {
        return of(visualSet, false);
    }

    public static JFreeChart of(VisualSet visualSet, boolean stacked) {
        return PlotUtils.chartFromXYTable(visualSet, stacked, VisualSet::xy);
    }

}
