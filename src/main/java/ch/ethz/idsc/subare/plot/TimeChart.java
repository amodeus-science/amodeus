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
        JFreeChart chart = PlotUtils.chartFromXYTable(visualSet, stacked, VisualSet::timed);

        DateAxis domainAxis = new DateAxis();
        domainAxis.setLabel(visualSet.getDomainAxisLabel());
        domainAxis.setTickUnit(new DateTickUnit(DateTickUnitType.SECOND, 1));
        domainAxis.setAutoTickUnitSelection(true);
        chart.getXYPlot().setDomainAxis(domainAxis);

        return chart;
    }

}
