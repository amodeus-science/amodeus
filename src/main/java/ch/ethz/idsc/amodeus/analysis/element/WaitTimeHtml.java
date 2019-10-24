/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.Collections;
import java.util.Map;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.WaitTimeHistoImage;
import ch.ethz.idsc.amodeus.analysis.report.HtmlBodyElement;
import ch.ethz.idsc.amodeus.analysis.report.HtmlReportElement;

public enum WaitTimeHtml implements HtmlReportElement {
    INSTANCE;

    private static final String IMAGE_FOLDER = "../data"; // relative to report folder

    @Override
    public Map<String, HtmlBodyElement> process(AnalysisSummary analysisSummary) {
        TravelTimeAnalysis travelTimeAnalysis = analysisSummary.getTravelTimeAnalysis();
        HtmlBodyElement aRElement = new HtmlBodyElement();
        aRElement.getHTMLGenerator().insertTextLeft(Quantiles.lblString("Wait Times"));
        aRElement.getHTMLGenerator().insertTextLeft(Quantiles.formatAggregates(travelTimeAnalysis.getWaitAggrgte()));
        aRElement.getHTMLGenerator().newLine();
        aRElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + WaitTimeHistoImage.FILENAME + ".png", 800, 600);
        aRElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + BinnedWaitingTimesImage.FILE_PNG, 800, 600);
        aRElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + WaitingCustomerExport.FILENAME + ".png", 800, 600);

        // TODO Who? also distribution over time bins?
        // aRElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + RequestsPerWaitingTimeImage.FILENAME + ".png", 800, 600);
        return Collections.singletonMap("", aRElement);
    }
}
