/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.data.xy.TableXYDataset;

/* package */ enum JFreeCharts {
    ;

    public static void formatLines(VisualSet visualSet, AbstractRenderer abstractRenderer) {
        for (int index = 0; index < visualSet.visualRows().size(); ++index) {
            VisualRow visualRow = visualSet.getVisualRow(index);
            abstractRenderer.setSeriesPaint(index, visualRow.getColor());
            abstractRenderer.setSeriesStroke(index, visualRow.getStroke());
            abstractRenderer.setSeriesOutlinePaint(index, visualRow.getColor().darker());
        }
    }

    public static JFreeChart fromXYTable(VisualSet visualSet, boolean stacked, TableXYDataset tableXYDataset) {
        JFreeChart jFreeChart = stacked //
                ? ChartFactory.createStackedXYAreaChart( //
                        visualSet.getPlotLabel(), //
                        visualSet.getDomainAxisLabel(), //
                        visualSet.getRangeAxisLabel(), //
                        tableXYDataset, //
                        PlotOrientation.VERTICAL, visualSet.hasLegend(), true, false)
                : ChartFactory.createXYLineChart( //
                        visualSet.getPlotLabel(), //
                        visualSet.getDomainAxisLabel(), //
                        visualSet.getRangeAxisLabel(), //
                        tableXYDataset, //
                        PlotOrientation.VERTICAL, visualSet.hasLegend(), true, false);

        formatLines(visualSet, (AbstractXYItemRenderer) jFreeChart.getXYPlot().getRenderer());

        return jFreeChart;
    }
}
