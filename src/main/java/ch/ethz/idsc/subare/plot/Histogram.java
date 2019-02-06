package ch.ethz.idsc.subare.plot;

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
        JFreeChart chart = ChartFactory.createBarChart(visualSet.getPlotLabel(), //
                visualSet.getDomainAxisLabel(), visualSet.getRangeAxisLabel(), //
                visualSet.categorical(), //
                PlotOrientation.VERTICAL, visualSet.hasLegend(), true, false);

        BarRenderer renderer = stacked ? new StackedBarRenderer() : new BarRenderer();
        renderer.setDrawBarOutline(true);
        PlotUtils.formatLines(visualSet, renderer);
        chart.getCategoryPlot().setRenderer(renderer);

        return chart;
    }

}
