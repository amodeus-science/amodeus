/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.element;

import java.io.File;

import org.jfree.chart.JFreeChart;

import amodeus.amodeus.analysis.AnalysisSummary;
import amodeus.amodeus.analysis.plot.AmodeusChartUtils;
import amodeus.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Transpose;
import amodeus.tensor.fig.StackedTimedChart;
import amodeus.tensor.fig.VisualRow;
import amodeus.tensor.fig.VisualSet;
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
                    ? distances.get(i).negate()
                    : distances.get(i);
            values = AnalysisMeanFilter.of(values);
            VisualRow visualRow = visualSet.add(de.time, values);
            visualRow.setLabel(StaticHelper.descriptions()[i]);
        }

        visualSet.setPlotLabel("Distance Distribution over Day");
        visualSet.setAxesLabelY(String.format("Distance [%s]", DistanceElement.getDistanceUnit()));

        JFreeChart chart = StackedTimedChart.of(visualSet);

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
