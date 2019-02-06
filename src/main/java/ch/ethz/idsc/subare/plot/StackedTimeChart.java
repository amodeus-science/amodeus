package ch.ethz.idsc.subare.plot;

import org.jfree.chart.JFreeChart;

public enum StackedTimeChart {
    ;

    public static JFreeChart of(VisualSet visualSet) {
        return TimeChart.of(visualSet, true);
    }

}
