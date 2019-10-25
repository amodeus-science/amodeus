/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.tensor.fig;

import org.jfree.chart.JFreeChart;

/** plotting utility */
public enum StackedTablePlot {
    ;

    public static JFreeChart of(VisualSet visualSet) {
        return JFreeCharts.stackedAreaPlot(visualSet, StaticHelper.categoryTableXYDataset(visualSet));
    }

}
