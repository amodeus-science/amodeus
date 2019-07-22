/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.StackedDistanceChartImage;
import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;
import ch.ethz.idsc.tensor.RationalScalar;

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
        aRElement.getHTMLGenerator().insertTextLeft("\nDistance Ratio:" + //
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
        aRElement.getHTMLGenerator().insertTextLeft("\n" + DECIMAL.format(de.totalDistanceRatio.multiply(RationalScalar.of(100, 1)) .number().doubleValue()) + "%" + //
                "\n" + DECIMAL.format(de.avgOccupancy.multiply(RationalScalar.of(100, 1)).number().doubleValue()) + " %" + //
                "\n\n" + //
                "\n" + DECIMAL.format(de.totalDistance.number().doubleValue()) + " km" + //
                "\n" + DECIMAL.format(de.totalDistanceRebal.number().doubleValue()) + " km (" + //
                DECIMAL.format(de.totalDistanceRebal.divide(de.totalDistance).multiply(RationalScalar.of(100, 1)).number().doubleValue()) + "%)" + //
                "\n" + DECIMAL.format(de.totalDistancePicku.number().doubleValue()) + " km (" + //
                DECIMAL.format(de.totalDistancePicku.divide(de.totalDistance).multiply(RationalScalar.of(100, 1)).number().doubleValue()) + "%)" + //
                "\n" + DECIMAL.format(de.totalDistanceWtCst.number().doubleValue()) + " km (" + //
                DECIMAL.format(de.totalDistanceWtCst.divide(de.totalDistance).multiply(RationalScalar.of(100, 1)).number().doubleValue() + "%)" + //
                        "\n" + //
                        "\n" + DECIMAL.format(de.totalDistanceWtCst.divide(RationalScalar.of(de.requestIndices.size(), 1)).number().doubleValue()) + " km"));
        File img = new File(IMAGE_FOLDER, StackedDistanceChartImage.FILENAME + ".png");
        aRElement.getHTMLGenerator() //
                .insertImg(img.getPath(), StackedDistanceChartImage.WIDTH, StackedDistanceChartImage.HEIGHT);
        bodyElements.put(aRKey, aRElement);
        return bodyElements;
    }
}
