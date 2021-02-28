/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.element;

import java.io.File;

import org.jfree.chart.JFreeChart;

import amodeus.amodeus.analysis.AnalysisSummary;
import amodeus.amodeus.analysis.plot.AmodeusChartUtils;
import amodeus.amodeus.util.math.GlobalAssert;
import amodeus.tensor.fig.TimedChart;
import amodeus.tensor.fig.VisualRow;
import amodeus.tensor.fig.VisualSet;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum BinnedWaitingTimesImage implements AnalysisExport {
    INSTANCE;

    public static final String FILE_PNG = "binnedWaitingTimes.png";
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 750;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        TravelTimeAnalysis tta = analysisSummary.getTravelTimeAnalysis();
        Scalar scalingFactor = RealScalar.of(60.0); // [s] to [min]

        VisualSet visualSet = new VisualSet(colorDataIndexed);
        for (int i = 0; i < Quantiles.LBL.length; ++i) {
            Tensor values = tta.waitTimePlotValues.get(Tensor.ALL, i).divide(scalingFactor);
            values = AnalysisMeanFilter.of(values);
            VisualRow visualRow = visualSet.add(tta.time, values);
            visualRow.setLabel(Quantiles.LBL[i]);
        }

        visualSet.setPlotLabel("Binned Waiting Times");
        visualSet.setAxesLabelX("Time");
        visualSet.setAxesLabelY("Waiting Times [min]");

        JFreeChart jFreeChart = TimedChart.of(visualSet);
        jFreeChart.getXYPlot().getRangeAxis().setRange(0., //
                tta.getWaitAggrgte().Get(2).divide(scalingFactor).number().doubleValue());

        try {
            File fileChart = new File(relativeDirectory, FILE_PNG);
            AmodeusChartUtils.saveAsPNG(jFreeChart, fileChart.toString(), WIDTH, HEIGHT);
            GlobalAssert.that(fileChart.isFile());
            System.out.println("Exported " + FILE_PNG);
        } catch (Exception e) {
            System.err.println("Plotting " + FILE_PNG + " failed");
            e.printStackTrace();
        }
    }
}
