package ch.ethz.idsc.subare.plot;

import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;

public enum Histogram {
    ;

    public static JFreeChart of(VisualSet visualSet) {
        return of(visualSet, false);
    }

    public static JFreeChart of(VisualSet visualSet, boolean stacked) {

        // TODO set title, label axis, etc.; maybe create shared PlotUtils

        final boolean isLegend = visualSet.visualRows().stream().anyMatch(visualRow -> //
                StringUtils.isNotEmpty(visualRow.getLabelString()) );
        JFreeChart chart = ChartFactory.createBarChart(visualSet.getPlotLabel(), //
                visualSet.getDomainAxisLabel(), visualSet.getRangeAxisLabel(), //
                visualSet.categorical(), //
                PlotOrientation.VERTICAL, isLegend, false, false);

        BarRenderer renderer = stacked ? new StackedBarRenderer() : new BarRenderer();
        renderer.setDrawBarOutline(true);
        for (int i = 0; i < visualSet.visualRows().size(); i++) {
            VisualRow visualRow = visualSet.get(i);
            renderer.setSeriesPaint(i, visualRow.getColor());
            renderer.setSeriesOutlinePaint(i, visualRow.getColor().darker());
            renderer.setSeriesOutlineStroke(i, visualRow.getStroke());
        }
        chart.getCategoryPlot().setRenderer(renderer);

        return chart;
    }

}
