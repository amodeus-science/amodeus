package ch.ethz.idsc.subare.plot;

import ch.ethz.idsc.tensor.Scalar;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;

import java.util.function.Function;

public enum Histogram {
    ;

    public static JFreeChart of(VisualSet visualSet) {
        return of(visualSet, false);
    }

    public static JFreeChart of(VisualSet visualSet, boolean stacked) {
        return of(visualSet, stacked, visualSet.visualRows().stream().allMatch( //
                r -> r.getValues().length() == 1 && visualSet.get(0).getDomain().equals(r.getDomain()) //
        ) ? s -> "" : Scalar::toString);
    }

    public static JFreeChart of(VisualSet visualSet, Function<Scalar, String> naming) {
        return of(visualSet, false, naming);
    }

    public static JFreeChart of(VisualSet visualSet, boolean stacked, Function<Scalar, String> naming) {
        JFreeChart chart = ChartFactory.createBarChart(visualSet.getPlotLabel(), //
                visualSet.getDomainAxisLabel(), visualSet.getRangeAxisLabel(), //
                visualSet.categorical(naming), //
                PlotOrientation.VERTICAL, visualSet.hasLegend(), true, false);

        BarRenderer renderer = stacked ? new StackedBarRenderer() : new BarRenderer();
        renderer.setDrawBarOutline(true);
        PlotUtils.formatLines(visualSet, renderer);
        chart.getCategoryPlot().setRenderer(renderer);

        return chart;
    }

}
