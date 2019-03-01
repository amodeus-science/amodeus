/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.plot;

import org.jfree.chart.JFreeChart;

/** inspired by
 * <a href="https://reference.wolfram.com/language/ref/ListPlot.html">ListPlot</a> */
public enum ListPlot {
    ;

    public static JFreeChart of(VisualSet visualSet) {
        return JFreeCharts.lineChart(visualSet, StaticHelper.xySeriesCollection(visualSet));
    }

}
