/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.awt.Color;
import java.io.File;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.ui.RectangleEdge;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum StackedTimeChart {
    ;

    private static final int WIDTH = DiagramSettings.WIDTH; /* Width of the image */
    private static final int HEIGHT = DiagramSettings.HEIGHT; /* Height of the image */

    public static void of(File directory, String fileTitle, String diagramTitle, //
            boolean filter, int filterSize, Double[] scale, //
            String[] labels, String yAxisLabel, Tensor time, Tensor values, ColorDataIndexed colorDataIndexed) throws Exception {

        GlobalAssert.that(time.length() == values.length());
        GlobalAssert.that(Transpose.of(values).length() == labels.length);
        GlobalAssert.that(Transpose.of(values).length() == scale.length);

        // filter if required
        Tensor valuesPlot = filter ? StaticHelper.filtered(values, filterSize) : values;

        // fill data to plotting function
        final TimeTableXYDataset dataset = new TimeTableXYDataset();
        double dataPoint;
        for (int i = 0; i < Transpose.of(valuesPlot).length(); i++) {
            for (int j = 0; j < time.length(); j++) {
                dataPoint = valuesPlot.get(j).Get(i).number().doubleValue() * scale[i];
                dataset.add(StaticHelper.toTime(time.Get(j).number().doubleValue()), //
                        dataPoint, labels[i]);
            }
        }

        JFreeChart timechart = ChartFactory.createStackedXYAreaChart(diagramTitle, "Time", //
                yAxisLabel, dataset, PlotOrientation.VERTICAL, false, false, false);

        timechart.getPlot().setBackgroundPaint(Color.white);
        timechart.getXYPlot().setRangeGridlinePaint(Color.lightGray);
        timechart.getXYPlot().setDomainGridlinePaint(Color.lightGray);

        DateAxis domainAxis = new DateAxis();
        domainAxis.setTickUnit(new DateTickUnit(DateTickUnitType.SECOND, 1));
        timechart.getXYPlot().setDomainAxis(domainAxis);
        timechart.getXYPlot().getDomainAxis().setAutoTickUnitSelection(true);

        for (int i = 0; i < labels.length; i++) {
            timechart.getXYPlot().getRenderer().setSeriesPaint(i, colorDataIndexed.getColor(i));
        }

        LegendTitle legend = new LegendTitle(timechart.getXYPlot().getRenderer());
        // legend.setItemFont(DiagramSettings.FONT_TICK);
        legend.setPosition(RectangleEdge.TOP);
        timechart.addLegend(legend);

        StaticHelper.savePlot(directory, fileTitle, timechart, WIDTH, HEIGHT);
    }

}