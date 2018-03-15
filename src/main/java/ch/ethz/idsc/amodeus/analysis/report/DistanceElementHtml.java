/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.StackedDistanceChartImage;
import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;
import ch.ethz.idsc.tensor.red.Mean;

public class DistanceElementHtml implements HtmlReportElement {

    private static final String IMAGE_FOLDER = "../data"; // relative to report folder
    private static final DecimalFormat DECIMAL = new DecimalFormat("#0.00");

    @Override
    public Map<String, HtmlBodyElement> process(AnalysisSummary analysisSummary) {
        DistanceElement de = analysisSummary.getDistanceElement();
        Map<String, HtmlBodyElement> bodyElements = new HashMap<>();
        // Aggregated Results:
        String aRKey = BodyElementKeys.AGGREGATERESULTS;
        HtmlBodyElement aRElement = new HtmlBodyElement();
        aRElement.getHTMLGenerator().insertTextLeft("\nDistance Ratio:" + //
                "\nOccupancy Ratio:" + //
                "\n" + //
                "\n" + aRElement.getHTMLGenerator().bold("Distances") + //
                "\n\tTotal:" + //
                "\n\tRebalancing:" + //
                "\n\tPickup:" + //
                "\n\tWith Customer:" + //
                "\n" + //
                "\nAverage Trip Distance:" //
        );
        aRElement.getHTMLGenerator().insertTextLeft("\n" + DECIMAL.format(de.totalDistanceRatio * 100) + "%" + //
                "\n" + DECIMAL.format(Mean.of(de.occupancyTensor).Get().number().doubleValue() * 100) + " %" + //
                "\n\n" + //
                "\n" + DECIMAL.format(de.totalDistance) + " km" + //
                "\n" + DECIMAL.format(de.totalDistanceRebal) + " km (" + //
                DECIMAL.format(100 * de.totalDistanceRebal / de.totalDistance) + "%)" + //
                "\n" + DECIMAL.format(de.totalDistancePicku) + " km (" + //
                DECIMAL.format(100 * de.totalDistancePicku / de.totalDistance) + "%)" + //
                "\n" + DECIMAL.format(de.totalDistanceWtCst) + " km (" + //
                DECIMAL.format(100 * de.totalDistanceWtCst / de.totalDistance) + "%)" + //
                "\n" + //
                "\n" + DECIMAL.format(de.totalDistanceWtCst / de.requestIndices.size()) + " km");
        File img = new File(IMAGE_FOLDER, StackedDistanceChartImage.FILENAME + ".png");
        aRElement.getHTMLGenerator().insertImgRight(img.getPath(), 250, 400);
        bodyElements.put(aRKey, aRElement);
        return bodyElements;
    }
}
