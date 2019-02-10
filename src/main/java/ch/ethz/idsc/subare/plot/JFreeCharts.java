/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.plot;

import java.util.function.Function;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.TableXYDataset;

import ch.ethz.idsc.tensor.Scalar;

/* package */ enum JFreeCharts {
    ;

    public static JFreeChart barChart(VisualSet visualSet, boolean stacked, Function<Scalar, String> naming) {
        JFreeChart jFreeChart = ChartFactory.createBarChart( //
                visualSet.getPlotLabel(), //
                visualSet.getDomainAxisLabel(), //
                visualSet.getRangeAxisLabel(), //
                StaticHelper.defaultCategoryDataset(visualSet, naming), //
                PlotOrientation.VERTICAL, visualSet.hasLegend(), true, false);

        BarRenderer barRenderer = stacked //
                ? new StackedBarRenderer()
                : new BarRenderer();
        barRenderer.setDrawBarOutline(true);
        formatLines(visualSet, barRenderer);
        jFreeChart.getCategoryPlot().setRenderer(barRenderer);

        return jFreeChart;
    }

    public static JFreeChart lineChart(VisualSet visualSet, IntervalXYDataset intervalXYDataset) {
        JFreeChart jFreeChart = ChartFactory.createXYLineChart( //
                visualSet.getPlotLabel(), //
                visualSet.getDomainAxisLabel(), //
                visualSet.getRangeAxisLabel(), //
                intervalXYDataset, //
                PlotOrientation.VERTICAL, visualSet.hasLegend(), true, false);

        formatLines(visualSet, (AbstractXYItemRenderer) jFreeChart.getXYPlot().getRenderer());

        return jFreeChart;
    }

    public static JFreeChart stackedAreaPlot(VisualSet visualSet, TableXYDataset tableXYDataset) {
        if (!visualSet.hasCommonDomain())
            System.out.println("WARNING: Having different domains might lead to unsatisfying results!");
        JFreeChart jFreeChart = ChartFactory.createStackedXYAreaChart( //
                visualSet.getPlotLabel(), //
                visualSet.getDomainAxisLabel(), //
                visualSet.getRangeAxisLabel(), //
                tableXYDataset, //
                PlotOrientation.VERTICAL, visualSet.hasLegend(), true, false);

        formatLines(visualSet, (AbstractXYItemRenderer) jFreeChart.getXYPlot().getRenderer());

        return jFreeChart;
    }

    // helper function
    private static void formatLines(VisualSet visualSet, AbstractRenderer abstractRenderer) {
        for (int index = 0; index < visualSet.visualRows().size(); ++index) {
            VisualRow visualRow = visualSet.getVisualRow(index);
            abstractRenderer.setSeriesPaint(index, visualRow.getColor());
            abstractRenderer.setSeriesStroke(index, visualRow.getStroke());
            abstractRenderer.setSeriesOutlinePaint(index, visualRow.getColor().darker());
        }
    }
}
