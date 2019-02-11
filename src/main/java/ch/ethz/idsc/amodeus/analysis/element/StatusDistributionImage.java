/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.subare.plot.VisualRow;
import ch.ethz.idsc.subare.plot.VisualSet;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.MeanFilter;

public enum StatusDistributionImage implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "statusDistribution";
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 750;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        String[] statusLabels = StaticHelper.descriptions();
        StatusDistributionElement st = analysisSummary.getStatusDistribution();

        VisualSet visualSet = new VisualSet(colorDataIndexed);
        for (int i = 0; i < statusLabels.length; i++) {
            Tensor values = Transpose.of(st.statusTensor).get(i);
            values = StaticHelper.FILTER_ON ? MeanFilter.of(values, StaticHelper.FILTERSIZE) : values;
            VisualRow visualRow = visualSet.add(st.time, values);
            visualRow.setLabel(statusLabels[i]);
        }

        visualSet.setPlotLabel("Status Distribution");
        visualSet.setRangeAxisLabel("RoboTaxis");

        JFreeChart chart = ch.ethz.idsc.subare.plot.StackedTimedChart.of(visualSet);

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
