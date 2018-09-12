/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

import java.util.HashMap;
import java.util.Map;

import org.matsim.core.utils.misc.Time;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.BinnedWaitingTimesImage;
import ch.ethz.idsc.amodeus.analysis.RequestsPerWaitingTimeImage;
import ch.ethz.idsc.amodeus.analysis.element.WaitingTimesElement;

public enum WaitingTimesHtml implements HtmlReportElement {
    INSTANCE;

    private static final String IMAGE_FOLDER = "../data"; // relative to report folder

    @Override
    public Map<String, HtmlBodyElement> process(AnalysisSummary analysisSummary) {
        WaitingTimesElement wt = analysisSummary.getWaitingTimes();
        // Waiting Times:
        HtmlBodyElement aRElement = new HtmlBodyElement();
        aRElement.getHTMLGenerator().insertTextLeft(aRElement.getHTMLGenerator().bold("Waiting Times") + //
                "\n\tMean:" + //
                "\n\t" + WaitingTimesElement.QUANTILE1 * 100 + "% quantile:" + //
                "\n\t" + WaitingTimesElement.QUANTILE2 * 100 + "% quantile:" + //
                "\n\t" + WaitingTimesElement.QUANTILE3 * 100 + "% quantile:" + //
                "\n\tMaximum:" //
        );
        aRElement.getHTMLGenerator().insertTextLeft(" " + //
                "\n" + Time.writeTime(wt.totalWaitTimeMean.number().doubleValue()) + //
                "\n" + Time.writeTime(wt.totalWaitTimeQuantile.Get(0).number().doubleValue()) + //
                "\n" + Time.writeTime(wt.totalWaitTimeQuantile.Get(1).number().doubleValue()) + //
                "\n" + Time.writeTime(wt.totalWaitTimeQuantile.Get(2).number().doubleValue()) + //
                "\n" + Time.writeTime(wt.maximumWaitTime)//
        );
        aRElement.getHTMLGenerator().newLine();
        aRElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + BinnedWaitingTimesImage.FILENAME + ".png", 800, 600);
        aRElement.getHTMLGenerator().insertImg(IMAGE_FOLDER + "/" + RequestsPerWaitingTimeImage.FILENAME + ".png", 800, 600);

        Map<String, HtmlBodyElement> bodyElements = new HashMap<>();
        bodyElements.put(BodyElementKeys.WAITINGTIMES, aRElement);
        return bodyElements;
    }

}
