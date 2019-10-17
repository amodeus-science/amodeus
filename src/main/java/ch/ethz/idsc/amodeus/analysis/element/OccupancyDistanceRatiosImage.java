/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.fig.VisualRow;
import ch.ethz.idsc.tensor.fig.VisualSet;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum OccupancyDistanceRatiosImage implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "occAndDistRatios";
    private static final String[] RATIOS_LABELS = new String[] { "occupancy ratio", "distance ratio" };
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 750;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        DistanceElement de = analysisSummary.getDistanceElement();

        VisualSet visualSet = new VisualSet(colorDataIndexed);
        for (int i = 0; i < RATIOS_LABELS.length; ++i) {
            Tensor values = de.ratios.get(Tensor.ALL, i);
            values = AnalysisMeanFilter.of(values);
            VisualRow visualRow = visualSet.add(de.time, values);
            visualRow.setLabel(RATIOS_LABELS[i]);
        }

        visualSet.setPlotLabel("Occupancy and Distance Ratios");
        visualSet.setAxesLabelX("Time");
        visualSet.setAxesLabelY("Occupancy / Distance Ratio");

        JFreeChart chart = ch.ethz.idsc.tensor.fig.TimedChart.of(visualSet);
        chart.getXYPlot().getRangeAxis().setRange(0., 1.);

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
