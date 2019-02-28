package ch.ethz.idsc.amodeus.analysis.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.util.Objects;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum XYChart {
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
            String[] labels, String xAxisLabel, String yAxisLabel, Tensor xValues, Tensor yValues, //
            Double[] maxRange, ColorDataIndexed colorDataIndexed) throws Exception {

        GlobalAssert.that(xValues.length() == yValues.length());
        GlobalAssert.that(Transpose.of(yValues).length() == labels.length);
        GlobalAssert.that(Transpose.of(yValues).length() == scale.length);

        final XYSeriesCollection dataset = new XYSeriesCollection( );
        Tensor valuesPlot = filter ? StaticHelper.filtered(yValues, filterSize) : yValues;

        double xDataPoint;
        double yDataPoint;
        for (int i = 0; i < Transpose.of(valuesPlot).length(); i++) {
        	final XYSeries xySeries = new XYSeries( labels[i] );
            for (int j = 0; j < xValues.length(); ++j) {
                xDataPoint = xValues.Get(j).number().doubleValue();
                yDataPoint = valuesPlot.get(j).Get(i).number().doubleValue() * scale[i];
                xySeries.add(xDataPoint, yDataPoint);
            }
            dataset.addSeries(xySeries);
        }

        JFreeChart xylineChart = ChartFactory.createXYLineChart(diagramTitle, xAxisLabel, yAxisLabel, //
                dataset, PlotOrientation.VERTICAL , false, false, false);
        System.out.println("requestsPerRoboTaxi");
        if (Objects.nonNull(maxRange)) {
            GlobalAssert.that(maxRange[0] < maxRange[1]);
            xylineChart.getXYPlot().getRangeAxis().setRange(maxRange[0], maxRange[1]);
        }

        xylineChart.getPlot().setBackgroundPaint(Color.WHITE);
        xylineChart.getXYPlot().setRangeGridlinePaint(Color.LIGHT_GRAY);
        xylineChart.getXYPlot().setDomainGridlinePaint(Color.LIGHT_GRAY);

        /** line thickness */
        for (int k = 0; k < xValues.length(); ++k) {
        	xylineChart.getXYPlot().getRenderer().setSeriesStroke(k, new BasicStroke(2.0f));
        }

        /** color themes, adapt colors & style */
        for (int i = 0; i < labels.length; ++i) {
        	xylineChart.getXYPlot().getRenderer().setSeriesPaint(i, colorDataIndexed.getColor(i));
        }

        LegendTitle legend = new LegendTitle(xylineChart.getXYPlot().getRenderer());
        legend.setPosition(RectangleEdge.TOP);
        xylineChart.addLegend(legend);

        /** save plot as png */
        StaticHelper.savePlot(directory, fileTitle, xylineChart, WIDTH, HEIGHT);
    }
}
