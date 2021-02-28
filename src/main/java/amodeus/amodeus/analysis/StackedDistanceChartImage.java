/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis;

import java.io.File;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

import amodeus.amodeus.analysis.element.AnalysisExport;
import amodeus.amodeus.analysis.element.DistanceElement;
import amodeus.amodeus.analysis.plot.AmodeusChartUtils;
import amodeus.amodeus.util.math.GlobalAssert;
import amodeus.tensor.fig.StackedHistogram;
import amodeus.tensor.fig.VisualSet;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum StackedDistanceChartImage implements AnalysisExport {
    INSTANCE;

    private static final Tensor DOMAIN = Tensors.vector(1);
    public static final String FILE_PNG = "stackedDistance.png";
    public static final int WIDTH = 700; /* Width of the image */
    public static final int HEIGHT = 125; /* Height of the image */

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        DistanceElement de = analysisSummary.getDistanceElement();
        VisualSet visualSet = new VisualSet(colorDataIndexed);
        visualSet.add(DOMAIN, Tensors.vector(de.totalDistanceWtCst.divide(de.totalDistance).number().doubleValue())).setLabel("With Customer");
        visualSet.add(DOMAIN, Tensors.vector(de.totalDistancePicku.divide(de.totalDistance).number().doubleValue())).setLabel("Pickup");
        visualSet.add(DOMAIN, Tensors.vector(de.totalDistanceRebal.divide(de.totalDistance).number().doubleValue())).setLabel("Rebalancing");

        visualSet.setPlotLabel("Total Distance Distribution");

        JFreeChart chart = StackedHistogram.of(visualSet);
        chart.getCategoryPlot().setOrientation(PlotOrientation.HORIZONTAL);
        chart.getCategoryPlot().getRangeAxis().setRange(0, 1.0);

        try {
            File fileChart = new File(relativeDirectory, FILE_PNG);
            AmodeusChartUtils.saveAsPNG(chart, fileChart.toString(), WIDTH, HEIGHT);
            GlobalAssert.that(fileChart.isFile());
            System.out.println("Exported " + FILE_PNG);
        } catch (Exception e) {
            System.err.println("Plotting " + FILE_PNG + " failed");
            e.printStackTrace();
        }
    }
}
