/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.shared;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.report.HtmlBodyElement;
import ch.ethz.idsc.amodeus.analysis.report.HtmlReportElement;

// TODO This class is not needed at the moment
public enum SharedAnalysisHtml implements HtmlReportElement {
    INSTANCE;

    private static final String IMAGE_FOLDER = "../data"; // relative to report folder
    private static final String BodyElementKey = "Shared Analysis";

    @Override
    public Map<String, HtmlBodyElement> process(AnalysisSummary analysisSummary) {
        Map<String, HtmlBodyElement> bodyElements = new HashMap<>();

        // Fleet Efficency
        HtmlBodyElement sharedElement = new HtmlBodyElement();
        sharedElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + NumberPassengerStatusDistribution.FILENAME + ".png", 800, 600);
        sharedElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + RideSharingDistributionCompositionStack.FILENAME + ".png", RideSharingDistributionCompositionStack.WIDTH,
                RideSharingDistributionCompositionStack.HEIGHT);
        // fEElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + ExtraDriveTimeImage.FILENAME + ".png", 800, 600);
        bodyElements.put(BodyElementKey, sharedElement);
        return bodyElements;
    }
}
