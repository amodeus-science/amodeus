package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.TravelTimeAnalysis;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum DriveTimeImages implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "requestsPerDriveTime";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        TravelTimeAnalysis travelTimeAnalysis = analysisSummary.getTravelTimeAnalysis();
        HistogramReportFigure.of(PositiveSubVector.of(travelTimeAnalysis.getDriveTimes()), //
                travelTimeAnalysis.getDrveAggrgte().Get(2), //
                colorDataIndexed, relativeDirectory, "Number of Requests per Drive Time", "Drive Times [s]", FILENAME);
    }
}
