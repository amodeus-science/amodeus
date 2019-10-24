/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.element.DistanceDistributionOverDayImage;
import ch.ethz.idsc.amodeus.analysis.element.OccupancyDistanceRatiosImage;
import ch.ethz.idsc.amodeus.analysis.shared.NumberPassengerStatusDistribution;
import ch.ethz.idsc.amodeus.analysis.shared.RideSharingDistributionCompositionStack;

public enum FleetEfficiencyHtml implements HtmlReportElement {
    INSTANCE;

    private static final String IMAGE_FOLDER = "../data"; // relative to report folder

    @Override
    public Map<String, HtmlBodyElement> process(AnalysisSummary analysisSummary) {
        Map<String, HtmlBodyElement> bodyElements = new HashMap<>();

        // Fleet Efficency
        HtmlBodyElement fEElement = new HtmlBodyElement();
        fEElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + DistanceDistributionOverDayImage.FILE_PNG, 800, 600);
        fEElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + OccupancyDistanceRatiosImage.FILE_PNG, 800, 600);
        fEElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + NumberPassengerStatusDistribution.IMAGE_NAME, 800, 600);
        fEElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + RideSharingDistributionCompositionStack.FILE_NAME, RideSharingDistributionCompositionStack.WIDTH,
                RideSharingDistributionCompositionStack.HEIGHT);
        bodyElements.put(BodyElementKeys.FLEETEFFICIENCY, fEElement);
        return bodyElements;
    }

}
