/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.subare.plot.TimedChart;
import ch.ethz.idsc.subare.plot.VisualRow;
import ch.ethz.idsc.subare.plot.VisualSet;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.MeanFilter;

public enum BinnedWaitingTimesImage implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "binnedWaitingTimes";
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 750;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        TravelTimeAnalysis tta = analysisSummary.getTravelTimeAnalysis();
        Scalar scalingFactor = RealScalar.of(60.0); // [s] to [min]

        VisualSet visualSet = new VisualSet(colorDataIndexed);
        for (int i = 0; i < Quantiles.LBL.length; ++i) {
            Tensor values = Transpose.of(tta.waitTimePlotValues).get(i).divide(scalingFactor);
            values = StaticHelper.FILTER_ON ? MeanFilter.of(values, StaticHelper.FILTERSIZE) : values;
            VisualRow visualRow = visualSet.add(tta.time, values);
            visualRow.setLabel(Quantiles.LBL[i]);
        }

        visualSet.setPlotLabel("Binned Waiting Times");
        visualSet.setDomainAxisLabel("Time");
        visualSet.setRangeAxisLabel("Waiting Times [min]");

        JFreeChart chart = TimedChart.of(visualSet);
        chart.getXYPlot().getRangeAxis().setRange(0., //
                tta.getWaitAggrgte().Get(2).divide(scalingFactor).number().doubleValue());

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
