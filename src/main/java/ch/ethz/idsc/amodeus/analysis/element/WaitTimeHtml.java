package ch.ethz.idsc.amodeus.analysis.element;

import java.util.HashMap;
import java.util.Map;

import org.matsim.core.utils.misc.Time;

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
        aRElement.getHTMLGenerator()
                .insertTextLeft(aRElement.getHTMLGenerator().bold("Wait Times") + //
                        "\n\t" + Quantiles.LBL[0] + //
                        "\n\t" + Quantiles.LBL[1] + //
                        "\n\t" + Quantiles.LBL[2] + //
                        "\n\t" + Quantiles.LBL[3] + //
                        "\n\tMaximum:" //
        );
        aRElement.getHTMLGenerator()
                .insertTextLeft(" " + //
                        "\n" + Time.writeTime(travelTimeAnalysis.getWaitAggrgte().get(0).Get(0).number().doubleValue()) + //
                        "\n" + Time.writeTime(travelTimeAnalysis.getWaitAggrgte().get(0).Get(1).number().doubleValue()) + //
                        "\n" + Time.writeTime(travelTimeAnalysis.getWaitAggrgte().get(0).Get(2).number().doubleValue()) + //
                        "\n" + Time.writeTime(travelTimeAnalysis.getWaitAggrgte().Get(1).number().doubleValue()) + //
                        "\n" + Time.writeTime(travelTimeAnalysis.getWaitAggrgte().Get(2).number().doubleValue()));

        aRElement.getHTMLGenerator().newLine();
        aRElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + WaitTimeHistoImage.FILENAME + ".png", 800, 600);
        aRElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + BinnedWaitingTimesImage.FILENAME + ".png", 800, 600);

        // TODO also distribution over time bins?
        // aRElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + RequestsPerWaitingTimeImage.FILENAME + ".png", 800, 600);
        Map<String, HtmlBodyElement> bodyElements = new HashMap<>();
        bodyElements.put("", aRElement);
        return bodyElements;
    }
}
