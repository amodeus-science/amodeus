/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.plot.ColorScheme;
import ch.ethz.idsc.amodeus.analysis.plot.TimeChart;

public enum BinnedWaitingTimesImage implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "binnedWaitingTimes";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorScheme colorScheme) {
        TravelTimeAnalysis tta = analysisSummary.getTravelTimeAnalysis();
        String xAxisLabel = "Time";
        String yAxisLabel = "Waiting Times [min]";
        double scalingFactor = 60.0; // [s] to [min]
        double[] scale = new double[] { 1.0 / scalingFactor, 1.0 / scalingFactor, 1.0 / scalingFactor, 1.0 / scalingFactor };

        try {
            TimeChart.of(relativeDirectory, FILENAME, "Binned Waiting Times", StaticHelper.FILTER_ON, StaticHelper.FILTERSIZE, //
                    scale, Quantiles.LBL, xAxisLabel, yAxisLabel, tta.time, tta.waitTimePlotValues, //
                    new Double[] { 0.0, tta.getWaitAggrgte().Get(2).number().doubleValue() / scalingFactor }, colorScheme);
        } catch (Exception e) {
            System.err.println("Binned Waiting Times Plot was unsucessfull!");
            e.printStackTrace();
        }
    }
}
