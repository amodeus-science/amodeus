/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.StackedDistanceChartImage;
import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;

public enum DistanceElementHtml implements HtmlReportElement {
    INSTANCE;

    private static final String IMAGE_FOLDER = "../data"; // relative to report folder
    private static final DecimalFormat DECIMAL = new DecimalFormat("#0.00");

    @Override
    public Map<String, HtmlBodyElement> process(AnalysisSummary analysisSummary) {
        DistanceElement de = analysisSummary.getDistanceElement();
        Map<String, HtmlBodyElement> bodyElements = new HashMap<>();
        // Aggregated Results:
        String aRKey = BodyElementKeys.AGGREGATERESULTS;
        HtmlBodyElement aRElement = new HtmlBodyElement();
        aRElement.getHTMLGenerator();
        aRElement.getHTMLGenerator()
                .insertTextLeft("\nDistance Ratio:" + //
                        "\nOccupancy Ratio:" + //
                        "\n" + //
                        "\n" + HtmlGenerator.bold("Distances") + //
                        "\n\tTotal:" + //
                        "\n\tRebalancing:" + //
                        "\n\tPickup:" + //
                        "\n\tWith Customer:" + //
                        "\n" + //
                        "\nAverage Trip Distance:" //
        );
        aRElement.getHTMLGenerator()
                .insertTextLeft("\n" + DECIMAL.format(de.totalDistanceRatio.number().doubleValue() * 100) + "%" + //
                        "\n" + DECIMAL.format(de.avgOccupancy.number().doubleValue() * 100) + " %" + //
                        "\n\n" + //
                        "\n" + DECIMAL.format(de.totalDistance.number().doubleValue()) + " km" + //
                        "\n" + DECIMAL.format(de.totalDistanceRebal.number().doubleValue()) + " km (" + //
                        DECIMAL.format(100 * de.totalDistanceRebal.number().doubleValue() / de.totalDistance.number().doubleValue()) + "%)" + //
                        "\n" + DECIMAL.format(de.totalDistancePicku.number().doubleValue()) + " km (" + //
                        DECIMAL.format(100 * de.totalDistancePicku.number().doubleValue() / de.totalDistance.number().doubleValue()) + "%)" + //
                        "\n" + DECIMAL.format(de.totalDistanceWtCst.number().doubleValue()) + " km (" + //
                        DECIMAL.format(100 * de.totalDistanceWtCst.number().doubleValue() / de.totalDistance.number().doubleValue()) + "%)" + //
                        "\n" + //
                        "\n" + DECIMAL.format(de.totalDistanceWtCst.number().doubleValue() / de.requestIndices.size()) + " km");
        File img = new File(IMAGE_FOLDER, StackedDistanceChartImage.FILENAME + ".png");
        aRElement.getHTMLGenerator() //
                .insertImg(img.getPath(), StackedDistanceChartImage.WIDTH, StackedDistanceChartImage.HEIGHT);
        bodyElements.put(aRKey, aRElement);
        return bodyElements;
    }
}
