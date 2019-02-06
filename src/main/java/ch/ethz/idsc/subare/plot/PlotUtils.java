package ch.ethz.idsc.subare.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.data.xy.TableXYDataset;

import java.util.function.Function;

/* package */ enum PlotUtils {
    ;

    public static void formatLines(VisualSet visualSet, AbstractRenderer renderer) {
        for (int i = 0; i < visualSet.visualRows().size(); i++) {
            VisualRow visualRow = visualSet.get(i);
            renderer.setSeriesPaint(i, visualRow.getColor());
            renderer.setSeriesOutlinePaint(i, visualRow.getColor().darker());
            renderer.setSeriesOutlineStroke(i, visualRow.getStroke());
        }
    }

    public static JFreeChart ofXYTable(VisualSet visualSet, boolean stacked, Function<VisualSet,TableXYDataset> table) {
        JFreeChart chart = stacked ? //
                ChartFactory.createStackedXYAreaChart(visualSet.getPlotLabel(), //
                        visualSet.getDomainAxisLabel(), visualSet.getRangeAxisLabel(), //
                        table.apply(visualSet), //
                        PlotOrientation.VERTICAL, visualSet.hasLegend(), true, false) : //
                ChartFactory.createXYLineChart(visualSet.getPlotLabel(), //
                        visualSet.getDomainAxisLabel(), visualSet.getRangeAxisLabel(), //
                        table.apply(visualSet), //
                        PlotOrientation.VERTICAL, visualSet.hasLegend(), true, false);

        PlotUtils.formatLines(visualSet, (AbstractXYItemRenderer) chart.getXYPlot().getRenderer());

        return chart;
    }
}
