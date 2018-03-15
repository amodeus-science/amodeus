/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.WaitingTimesElement;
import ch.ethz.idsc.amodeus.analysis.plot.TimeChart;

public class BinnedWaitingTimesImage implements AnalysisExport {
    public static final String FILENAME = "binnedWaitingTimes";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory) {
        WaitingTimesElement wt = analysisSummary.getWaitingTimes();

        String xAxisLabel = "Time";
        String yAxisLabel = "Waiting Times [min]";
        double scalingFactor = 60.0; // [s] to [min]
        double[] scale = new double[] { 1.0 / scalingFactor, 1.0 / scalingFactor, 1.0 / scalingFactor, 1.0 / scalingFactor };

        try {
            TimeChart.of( //
                    relativeDirectory, //
                    FILENAME, //
                    "Binned Waiting Times", //
                    StaticHelper.FILTER_ON, //
                    StaticHelper.FILTERSIZE, //
                    scale, //
                    WaitingTimesElement.WAITTIMES_LABELS, //
                    xAxisLabel, //
                    yAxisLabel, //
                    wt.time, //
                    wt.waitTimePlotValues, //
                    wt.maximumWaitTime / scalingFactor);
        } catch (Exception e) {
            System.err.println("Binned Waiting Times Plot was unsucessfull!");
            e.printStackTrace();
        }

    }

}
