/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.fig.StackedTimedChart;
import ch.ethz.idsc.tensor.fig.VisualRow;
import ch.ethz.idsc.tensor.fig.VisualSet;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum DistanceDistributionOverDayImage implements AnalysisExport {
    INSTANCE;

    public static final String FILE_PNG = "distanceDistribution.png";
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 750;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        DistanceElement de = analysisSummary.getDistanceElement();
        Tensor distances = Transpose.of(de.distancesOverDay).extract(1, 4);

        VisualSet visualSet = new VisualSet(colorDataIndexed);
        for (int i = 0; i < 3; i++) {
            Tensor values = i == 0 //
                    ? distances.get(i).negate() : distances.get(i);
            values = AnalysisMeanFilter.of(values);
            VisualRow visualRow = visualSet.add(de.time, values);
            visualRow.setLabel(StaticHelper.descriptions()[i]);
        }

        visualSet.setPlotLabel("Distance Distribution over Day");
        visualSet.setAxesLabelY(String.format("Distance [%s]", DistanceElement.TARGET_UNIT));

        JFreeChart chart = StackedTimedChart.of(visualSet);

        try {
            File fileChart = new File(relativeDirectory, FILE_PNG);
            ChartUtilities.saveChartAsPNG(fileChart, chart, WIDTH, HEIGHT);
            GlobalAssert.that(fileChart.isFile());
            System.out.println("Exported " + FILE_PNG);
        } catch (Exception e) {
            System.err.println("Plotting " + FILE_PNG + " failed");
            e.printStackTrace();
        }
    }

}
