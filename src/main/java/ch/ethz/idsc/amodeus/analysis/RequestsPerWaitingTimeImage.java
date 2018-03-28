/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.WaitingTimesElement;
import ch.ethz.idsc.amodeus.analysis.plot.ColorScheme;
import ch.ethz.idsc.amodeus.analysis.plot.DiagramSettings;
import ch.ethz.idsc.amodeus.analysis.plot.HistogramPlot;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.pdf.BinCounts;
import ch.ethz.idsc.tensor.sca.Round;

public class RequestsPerWaitingTimeImage implements AnalysisExport {
    public static final String FILENAME = "requestsPerWaitTime";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorScheme colorScheme) {
        WaitingTimesElement wt = analysisSummary.getWaitingTimes();

        int waitBinNumber = 30;
        Scalar waitBinNumberScaling = RealScalar.of(1.0 / waitBinNumber);
        Scalar waitBinSize = Round.of(RealScalar.of(wt.maximumWaitTime).multiply(waitBinNumberScaling));

        RealScalar.of(10.0);
        Tensor waitTimes = Tensors.empty();
        wt.requestWaitTimes.values().stream().forEach(v -> waitTimes.append(RealScalar.of(v)));
        Tensor waitBinCounter = BinCounts.of(//
                waitTimes, //
                waitBinSize);

        waitBinCounter = waitBinCounter.divide(RealScalar.of(wt.requestWaitTimes.size()));

        try {
            HistogramPlot.of( //
                    waitBinCounter.multiply(RealScalar.of(100)), //
                    relativeDirectory, //
                    FILENAME, //
                    "Number of Requests per Wait Time", //
                    waitBinSize.number().doubleValue(), //
                    "% of requests", //
                    "Waiting Times [s]", //
                    DiagramSettings.WIDTH, DiagramSettings.HEIGHT,
                    colorScheme);
        } catch (Exception e) {
            System.err.println("Plot of the Wait Times per Requests Failed");
            e.printStackTrace();
        }

    }

}
