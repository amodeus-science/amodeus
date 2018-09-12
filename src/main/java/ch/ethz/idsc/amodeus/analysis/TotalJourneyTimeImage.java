/*amodeus-Copyright(c)2018, ETH Zurich, Institute for Dynamic Systems and Control*/
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.TravelTimeAnalysis;
import ch.ethz.idsc.amodeus.analysis.plot.ColorScheme;

public enum TotalJourneyTimeImage implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "requestsPerTotalTravelTime";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorScheme colorScheme) {
        TravelTimeAnalysis travelTimeAnalysis = analysisSummary.getTravelTimeAnalysis();
        HistogramReportFigure.of(PositiveSubVector.of(travelTimeAnalysis.getTotalJourneyTimes()), travelTimeAnalysis.getTotJAggrgte().Get(2), //
                colorScheme, relativeDirectory, "Number of Requests per Total Journey Time", "Total Journey Times [s]", FILENAME);
    }
}
