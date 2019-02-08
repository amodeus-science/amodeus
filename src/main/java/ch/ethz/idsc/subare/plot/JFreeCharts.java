/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.plot;

import java.awt.Color;
import java.util.function.Function;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import ch.ethz.idsc.tensor.Tensor;

/* package */ enum JFreeCharts {
    ;

    public static void formatLines(VisualSet visualSet, AbstractRenderer renderer) {
        for (int i = 0; i < visualSet.visualRows().size(); ++i) {
            VisualRow visualRow = visualSet.get(i);
            renderer.setSeriesPaint(i, visualRow.getColor());
            renderer.setSeriesStroke(i, visualRow.getStroke());
            renderer.setSeriesOutlinePaint(i, visualRow.getColor().darker());
        }
    }

    public static JFreeChart chartFromXYTable(VisualSet visualSet, boolean stacked, Function<VisualSet, TableXYDataset> table) {
        JFreeChart chart = stacked //
                ? ChartFactory.createStackedXYAreaChart(visualSet.getPlotLabel(), //
                        visualSet.getDomainAxisLabel(), visualSet.getRangeAxisLabel(), //
                        table.apply(visualSet), //
                        PlotOrientation.VERTICAL, visualSet.hasLegend(), true, false)
                : ChartFactory.createXYLineChart(visualSet.getPlotLabel(), //
                        visualSet.getDomainAxisLabel(), visualSet.getRangeAxisLabel(), //
                        table.apply(visualSet), //
                        PlotOrientation.VERTICAL, visualSet.hasLegend(), true, false);

        JFreeCharts.formatLines(visualSet, (AbstractXYItemRenderer) chart.getXYPlot().getRenderer());

        return chart;
    }
}
