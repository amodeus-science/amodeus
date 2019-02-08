/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.plot;

import java.util.function.Function;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

/** inspired by
 * <a href="https://reference.wolfram.com/language/ref/Histogram.html">Histogram</a> */
public enum Histogram {
    ;

    public static JFreeChart of(VisualSet visualSet) {
        return of(visualSet, false);
    }

    public static JFreeChart of(VisualSet visualSet, boolean stacked) {
        Function<Scalar, String> naming = visualSet.visualRows().stream() //
                .allMatch(r -> r.points().length() == 1 //
                        && visualSet.get(0).points().get(Tensor.ALL, 0).equals(r.points().get(Tensor.ALL, 0))) //
                                ? s -> ""
                                : Scalar::toString;
        return of(visualSet, stacked, naming);
    }

    public static JFreeChart of(VisualSet visualSet, Function<Scalar, String> naming) {
        return of(visualSet, false, naming);
    }

    public static JFreeChart of(VisualSet visualSet, boolean stacked, Function<Scalar, String> naming) {
        JFreeChart chart = ChartFactory.createBarChart(visualSet.getPlotLabel(), //
                visualSet.getDomainAxisLabel(), visualSet.getRangeAxisLabel(), //
                visualSet.categorical(naming), //
                PlotOrientation.VERTICAL, visualSet.hasLegend(), true, false);

        BarRenderer renderer = stacked //
                ? new StackedBarRenderer()
                : new BarRenderer();
        renderer.setDrawBarOutline(true);
        JFreeCharts.formatLines(visualSet, renderer);
        chart.getCategoryPlot().setRenderer(renderer);

        return chart;
    }

}
