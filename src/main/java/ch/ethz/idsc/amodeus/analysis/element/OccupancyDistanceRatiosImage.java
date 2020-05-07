/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;

import org.jfree.chart.JFreeChart;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.plot.AmodeusChartUtils;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Dimensions;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.fig.VisualRow;
import ch.ethz.idsc.tensor.fig.VisualSet;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum OccupancyDistanceRatiosImage implements AnalysisExport {
    INSTANCE;

    public static final String FILE_PNG = "occAndDistRatios.png";
    private static final String[] RATIOS_LABELS = new String[] { "occupancy ratio", "distance ratio" };
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 750;

    @Override
    public void summaryTarget(AnalysisSummary summary, File relDir, ColorDataIndexed colorData) {
        DistanceElement de = summary.getDistanceElement();
        StatusDistributionElement sd = summary.getStatusDistribution();
        Tensor distanceRatios = de.distanceRatioOverDay;
        Tensor occupRatios = Transpose.of(sd.occupancyTensor).get(1);
        // put together
        Tensor ratios = Transpose.of(Tensors.of(occupRatios, distanceRatios));
        Tensor time = de.time.unmodifiable();
        compute(ratios, time, colorData, relDir);
    }

    /* package */ void compute(Tensor ratios, Tensor time, ColorDataIndexed colorData, File dir) {
        VisualSet visualSet = new VisualSet(colorData);

        /** The ratios must be in the format Transpose.of({{1,1,0.5,1},{0,0,1,2}})
         * The time must be inthe format {1,2,3,4} */
        GlobalAssert.that(Dimensions.of(ratios).size() == 2);
        GlobalAssert.that(Dimensions.of(ratios).get(1) == 2);
        GlobalAssert.that(Dimensions.of(time).size() == 1);
        GlobalAssert.that(Dimensions.of(ratios).get(0).equals(Dimensions.of(time).get(0)));

        for (int i = 0; i < RATIOS_LABELS.length; ++i) {
            Tensor values = ratios.get(Tensor.ALL, i);
            values = AnalysisMeanFilter.of(values);
            VisualRow visualRow = visualSet.add(time, values);
            visualRow.setLabel(RATIOS_LABELS[i]);
        }

        visualSet.setPlotLabel("Occupancy and Distance Ratios");
        visualSet.setAxesLabelX("Time");
        visualSet.setAxesLabelY("Occupancy / Distance Ratio");

        JFreeChart chart = ch.ethz.idsc.tensor.fig.TimedChart.of(visualSet);
        chart.getXYPlot().getRangeAxis().setRange(0., 1.);

        try {
            File fileChart = new File(dir, FILE_PNG);
            AmodeusChartUtils.saveAsPNG(chart, fileChart.toString(), WIDTH, HEIGHT);
            GlobalAssert.that(fileChart.isFile());
            System.out.println("Exported " + FILE_PNG);
        } catch (Exception e) {
            System.err.println("Plotting " + FILE_PNG + " failed");
            e.printStackTrace();
        }
    }
}
