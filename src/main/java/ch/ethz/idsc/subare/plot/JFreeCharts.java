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

    public static JFreeChart fromXYSeries(VisualSet visualSet) {
        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();

        for (VisualRow visualRow : visualSet.visualRows()) {
            String string = visualRow.getLabelString();
            Comparable key = string.isEmpty() //
                    ? xySeriesCollection.getSeriesCount()
                    : string;
            XYSeries xySeries = new XYSeries(key);
            for (Tensor point : visualRow.points())
                xySeries.add(point.Get(0).number(), point.Get(1).number());
            xySeriesCollection.addSeries(xySeries);

        }

        JFreeChart jFreeChart = ChartFactory.createXYLineChart( //
                visualSet.getPlotLabel(), //
                visualSet.getDomainAxisLabel(), //
                visualSet.getRangeAxisLabel(), //
                xySeriesCollection, PlotOrientation.VERTICAL, //
                false, // legend
                false, // tooltips
                false); // urls
        final XYPlot xyPlot = jFreeChart.getXYPlot();
        final XYItemRenderer xyItemRenderer = xyPlot.getRenderer();
        int limit = xySeriesCollection.getSeriesCount();
        for (int index = 0; index < limit; ++index) {
            VisualRow visualRow = visualSet.get(index);
            xyItemRenderer.setSeriesPaint(index, visualRow.getColor());
            xyItemRenderer.setSeriesStroke(index, visualRow.getStroke());
        }
        xyPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        xyPlot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        xyPlot.getDomainAxis().setLowerMargin(0.0);
        xyPlot.getDomainAxis().setUpperMargin(0.0);
        if (visualSet.hasLegend()) {
            LegendTitle legendTitle = new LegendTitle(xyItemRenderer);
            legendTitle.setPosition(RectangleEdge.TOP);
            jFreeChart.addLegend(legendTitle);
        }
        if (visualSet.axisClipX != null) {
            NumberAxis numberAxis = (NumberAxis) jFreeChart.getXYPlot().getDomainAxis();
            numberAxis.setRange(visualSet.axisClipX.min().number().doubleValue(), visualSet.axisClipY.max().number().doubleValue());
        }
        if (visualSet.axisClipY != null) {
            NumberAxis numberAxis = (NumberAxis) jFreeChart.getXYPlot().getRangeAxis();
            numberAxis.setRange(visualSet.axisClipY.min().number().doubleValue(), visualSet.axisClipY.max().number().doubleValue());
        }
        return jFreeChart;

    }
}
