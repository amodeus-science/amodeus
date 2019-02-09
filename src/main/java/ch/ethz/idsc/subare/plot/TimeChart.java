/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.plot;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;

public enum TimeChart {
    ;

    public static JFreeChart of(VisualSet visualSet) {
        return of(visualSet, false);
    }

    public static JFreeChart of(VisualSet visualSet, boolean stacked) {
        JFreeChart jFreeChart = JFreeCharts.fromXYTable(visualSet, stacked, StaticHelper.timeTableXYDataset(visualSet));
        DateAxis domainAxis = new DateAxis();
        domainAxis.setLabel(visualSet.getDomainAxisLabel());
        domainAxis.setTickUnit(new DateTickUnit(DateTickUnitType.SECOND, 1));
        domainAxis.setAutoTickUnitSelection(true);
        jFreeChart.getXYPlot().setDomainAxis(domainAxis);
        return jFreeChart;
    }

}
