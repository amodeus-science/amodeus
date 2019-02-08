/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.subare.plot.CompositionStack;
import ch.ethz.idsc.subare.plot.VisualRow;
import ch.ethz.idsc.subare.plot.VisualSet;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum StackedDistanceChartImage implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "stackedDistance";
    public static final int WIDTH = 700; /* Width of the image */
    public static final int HEIGHT = 125; /* Height of the image */

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        DistanceElement de = analysisSummary.getDistanceElement();
        VisualSet visualSet = new VisualSet( //
                new VisualRow(RealScalar.ONE, RealScalar.of(de.totalDistanceWtCst / de.totalDistance)), //
                new VisualRow(RealScalar.ONE, RealScalar.of(de.totalDistancePicku / de.totalDistance)), //
                new VisualRow(RealScalar.ONE, RealScalar.of(de.totalDistanceRebal / de.totalDistance)) //
        );
        visualSet.setPlotLabel("Total Distance Distribution");
        visualSet.setRowLabel(0, "With Customer");
        visualSet.setRowLabel(1, "Pickup");
        visualSet.setRowLabel(2, "Rebalancing");
        visualSet.setColors(colorDataIndexed);

        JFreeChart chart = CompositionStack.of(visualSet);
        chart.getCategoryPlot().setOrientation(PlotOrientation.HORIZONTAL);
        chart.getCategoryPlot().getRangeAxis().setRange(0, 1.0);

        try {
            File fileChart = new File(relativeDirectory, FILENAME + ".png");
            ChartUtilities.saveChartAsPNG(fileChart, chart, WIDTH, HEIGHT);
            GlobalAssert.that(fileChart.isFile());
            System.out.println("Exported " + FILENAME + ".png");
        } catch (Exception e) {
            System.err.println("Plotting " + FILENAME + " failed");
            e.printStackTrace();
        }
    }
}
