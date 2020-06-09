/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis;

import java.io.File;

import amodeus.amodeus.analysis.element.AnalysisExport;
import amodeus.amodeus.analysis.element.TravelTimeAnalysis;
import amodeus.amodeus.util.math.NonNegativeSubVector;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum WaitTimeHistoImage implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "requestsPerWaitTime";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        TravelTimeAnalysis travelTimeAnalysis = analysisSummary.getTravelTimeAnalysis();
        HistogramReportFigure.of( //
                NonNegativeSubVector.of(travelTimeAnalysis.getWaitTimes()), //
                travelTimeAnalysis.getWaitAggrgte().Get(2), //
                colorDataIndexed, relativeDirectory, "Number of Requests per Wait Time", "Wait Times [s]", FILENAME);
    }
}
