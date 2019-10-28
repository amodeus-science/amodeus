/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.util.Objects;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleEdge;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Unprotect;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

/** TODO this class was marked deprecated but replacement has not been carried out yet
 * see {@link ch.ethz.idsc.tensor.fig.TimedChart} */
public enum TimeChart {
    ;
    private static final int WIDTH = DiagramSettings.WIDTH;
    private static final int HEIGHT = DiagramSettings.HEIGHT;

    /** creates a .png figure in the @param directory with file name @param fileTitle and @param diagramTitle
     * 
     * * @param time are the values on the x-axis, on the y-axis the values @param values are plotted,
     * custom @param maxRange can be provided or null for native choice.
     * 
     * If @param filter is set to true, a moving average filter with size @param filterSize is used to smooth the data.
     * 
     * The values @param scale can be used to scale the data.
     * 
     * If different time series are used they can be labeled with @param labels, axis descriptions are set
     * to @param xAxisLabel and @param yAxisLabel, different @param colorScheme are implemented
     * 
     * @throws Exception if .png figure could not be saved */
    public static void of(File directory, String fileTitle, String diagramTitle, //
            boolean filter, int filterSize, double[] scale, //
            String[] labels, String xAxisLabel, String yAxisLabel, Tensor time, Tensor values, //
            double[] maxRange, ColorDataIndexed colorDataIndexed) throws Exception {

        GlobalAssert.that(time.length() == values.length());

        GlobalAssert.that(Unprotect.dimension1(values) == labels.length);
        GlobalAssert.that(labels.length == scale.length);

        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        Tensor valuesPlot = filter ? StaticHelper.filtered(values, filterSize) : values;

        double dataPoint;
        int dimension1 = Unprotect.dimension1(valuesPlot);
        for (int i = 0; i < dimension1; ++i) {
            final TimeSeries series = new TimeSeries(labels[i]);
            for (int j = 0; j < time.length(); ++j) {
                Second daytime = StaticHelper.toTime(time.Get(j).number().doubleValue());
                dataPoint = valuesPlot.Get(j, i).number().doubleValue() * scale[i];
                series.add(daytime, dataPoint);
            }
            dataset.addSeries(series);
        }

        JFreeChart timechart = ChartFactory.createTimeSeriesChart(diagramTitle, xAxisLabel, yAxisLabel, //
                dataset, false, false, false);

        if (Objects.nonNull(maxRange)) {
            GlobalAssert.that(maxRange[0] < maxRange[1]);
            timechart.getXYPlot().getRangeAxis().setRange(maxRange[0], maxRange[1]);
        }

        timechart.getPlot().setBackgroundPaint(Color.WHITE);
        timechart.getXYPlot().setRangeGridlinePaint(Color.LIGHT_GRAY);
        timechart.getXYPlot().setDomainGridlinePaint(Color.LIGHT_GRAY);

        /** line thickness */
        for (int k = 0; k < time.length(); ++k)
            timechart.getXYPlot().getRenderer().setSeriesStroke(k, new BasicStroke(2.0f));

        /** color themes, adapt colors & style */
        for (int i = 0; i < labels.length; ++i)
            timechart.getXYPlot().getRenderer().setSeriesPaint(i, colorDataIndexed.getColor(i));

        LegendTitle legend = new LegendTitle(timechart.getXYPlot().getRenderer());
        legend.setPosition(RectangleEdge.TOP);
        timechart.addLegend(legend);

        /** save plot as png */
        PlotSave.now(directory, fileTitle, timechart, WIDTH, HEIGHT);
    }
}