/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleEdge;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Transpose;

public enum TimeChart {
    ;

    private static final int WIDTH = DiagramSettings.WIDTH; /* Width of the image */
    private static final int HEIGHT = DiagramSettings.HEIGHT; /* Height of the image */

    public static void of(File directory, String fileTitle, String diagramTitle, //
            boolean filter, int filterSize, double[] scale, //
            String[] labels, String xAxisLabel, String yAxisLabel, Tensor time, Tensor values, double maxRange, ColorScheme colorScheme) throws Exception {

        // keep
        GlobalAssert.that(time.length() == values.length());
        GlobalAssert.that(Transpose.of(values).length() == labels.length);
        GlobalAssert.that(Transpose.of(values).length() == scale.length);

        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        Tensor valuesPlot = filter ? StaticHelper.filtered(values, filterSize) : values;

        double dataPoint;
        for (int i = 0; i < Transpose.of(valuesPlot).length(); i++) {
            final TimeSeries series = new TimeSeries(labels[i]);
            for (int j = 0; j < time.length(); ++j) {
                Second daytime = StaticHelper.toTime(time.Get(j).number().doubleValue());
                dataPoint = valuesPlot.get(j).Get(i).number().doubleValue() * scale[i];
                series.add(daytime, dataPoint);
            }
            dataset.addSeries(series);
        }

        JFreeChart timechart = ChartFactory.createTimeSeriesChart(diagramTitle, xAxisLabel, yAxisLabel, //
                dataset, false, false, false);

        // range and colors of the background/grid
        if (maxRange != -1.0)
            timechart.getXYPlot().getRangeAxis().setRange(0, maxRange);
        timechart.getPlot().setBackgroundPaint(Color.white);
        timechart.getXYPlot().setRangeGridlinePaint(Color.lightGray);
        timechart.getXYPlot().setDomainGridlinePaint(Color.lightGray);

        // line thickness
        for (int k = 0; k < time.length(); k++) {
            timechart.getXYPlot().getRenderer().setSeriesPaint(k, colorScheme.of(k));
            timechart.getXYPlot().getRenderer().setSeriesStroke(k, new BasicStroke(2.0f));
        }

        // Font Text are being set by the general ChartTheme loaded in Main Analysis Class
        // set text fonts
        // timechart.getTitle().setFont(DiagramSettings.FONT_TITLE);
        // timechart.getXYPlot().getDomainAxis().setLabelFont(DiagramSettings.FONT_AXIS);
        // timechart.getXYPlot().getRangeAxis().setLabelFont(DiagramSettings.FONT_AXIS);
        // timechart.getXYPlot().getDomainAxis().setTickLabelFont(DiagramSettings.FONT_TICK);
        // timechart.getXYPlot().getRangeAxis().setTickLabelFont(DiagramSettings.FONT_TICK);

        LegendTitle legend = new LegendTitle(timechart.getXYPlot().getRenderer());
        // legend.setItemFont(DiagramSettings.FONT_TICK);
        legend.setPosition(RectangleEdge.TOP);
        timechart.addLegend(legend);

        // save plot as png
        StaticHelper.savePlot(directory, fileTitle, timechart, WIDTH, HEIGHT);

    }

}
