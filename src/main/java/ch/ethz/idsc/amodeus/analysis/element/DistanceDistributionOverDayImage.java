/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.subare.plot.StackedTimeChart;
import ch.ethz.idsc.subare.plot.VisualRow;
import ch.ethz.idsc.subare.plot.VisualSet;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.MeanFilter;

public enum DistanceDistributionOverDayImage implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "distanceDistribution";
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 750;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        DistanceElement de = analysisSummary.getDistanceElement();
        Tensor distances = Transpose.of(de.distancesOverDay).extract(1, 4);

        VisualSet visualSet = new VisualSet();
        for (int i = 0; i < 3; i++) {
            Tensor values = i == 0 ? distances.get(i).multiply(RealScalar.of(-1)) : distances.get(i);
            values = StaticHelper.FILTER_ON ? MeanFilter.of(values, StaticHelper.FILTERSIZE) : values;
            VisualRow visualRow = new VisualRow(de.time, values);
            visualRow.setLabel(StaticHelper.descriptions()[i]);
            visualSet.add(visualRow);
        }

        visualSet.setPlotLabel("Distance Distribution over Day");
        visualSet.setRangeAxisLabel("Distance [km]");
        visualSet.setColors(colorDataIndexed);

        JFreeChart chart = StackedTimeChart.of(visualSet);

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
