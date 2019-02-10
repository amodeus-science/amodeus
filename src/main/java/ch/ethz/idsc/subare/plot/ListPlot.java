/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeriesCollection;

/** inspired by
 * <a href="https://reference.wolfram.com/language/ref/ListPlot.html">ListPlot</a> */
public enum ListPlot {
    ;

    public static JFreeChart of(VisualSet visualSet) {
        XYSeriesCollection xySeriesCollection = StaticHelper.xySeriesCollection(visualSet);
        JFreeChart jFreeChart = ChartFactory.createXYLineChart( //
                visualSet.getPlotLabel(), //
                visualSet.getDomainAxisLabel(), //
                visualSet.getRangeAxisLabel(), //
                xySeriesCollection, PlotOrientation.VERTICAL, //
                visualSet.hasLegend(), // legend
                false, // tooltips
                false); // urls
        XYPlot xyPlot = jFreeChart.getXYPlot();
        XYItemRenderer xyItemRenderer = xyPlot.getRenderer();
        int limit = xySeriesCollection.getSeriesCount();
        for (int index = 0; index < limit; ++index) {
            xyItemRenderer.setSeriesPaint(index, visualSet.getVisualRow(index).getColor());
            xyItemRenderer.setSeriesStroke(index, visualSet.getVisualRow(index).getStroke());
        }
        // if (visualSet.hasLegend()) {
        // LegendTitle legendTitle = new LegendTitle(xyItemRenderer);
        // // legendTitle.setPosition(RectangleEdge.TOP);
        // jFreeChart.addLegend(legendTitle);
        // }
        return jFreeChart;
    }

}
