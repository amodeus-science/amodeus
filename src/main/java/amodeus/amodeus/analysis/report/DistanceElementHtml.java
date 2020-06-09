/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.report;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import amodeus.amodeus.analysis.AnalysisSummary;
import amodeus.amodeus.analysis.StackedDistanceChartImage;
import amodeus.amodeus.analysis.element.DistanceElement;
import amodeus.amodeus.analysis.element.StatusDistributionElement;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.QuantityUnit;

public enum DistanceElementHtml implements HtmlReportElement {
    INSTANCE;

    private static final String IMAGE_FOLDER = "../data"; // relative to report folder
    private static final DecimalFormat DECIMAL = new DecimalFormat("#0.00");

    @Override
    public Map<String, HtmlBodyElement> process(AnalysisSummary analysisSummary) {
        DistanceElement de = analysisSummary.getDistanceElement();
        StatusDistributionElement sd = analysisSummary.getStatusDistribution();
        Map<String, HtmlBodyElement> bodyElements = new HashMap<>();
        // Aggregated Results:
        String aRKey = BodyElementKeys.AGGREGATERESULTS;
        HtmlBodyElement aRElement = new HtmlBodyElement();
        aRElement.getHTMLGenerator().insertTextLeft( //
                "\nDistance Ratio:" + //
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
        aRElement.getHTMLGenerator().insertTextLeft( //
                "\n" + DECIMAL.format(de.totalDistanceRatio.number().doubleValue() * 100) + "%" + //
                        "\n" + DECIMAL.format(sd.avgOccupancy.number().doubleValue() * 100) + "%" + //
                        "\n\n" + //
                        "\n" + format(de.totalDistance) + //
                        "\n" + format(de.totalDistanceRebal) + " (" + //
                        DECIMAL.format(100 * de.totalDistanceRebal.number().doubleValue() / de.totalDistance.number().doubleValue()) + "%)" + //
                        "\n" + format(de.totalDistancePicku) + " (" + //
                        DECIMAL.format(100 * de.totalDistancePicku.number().doubleValue() / de.totalDistance.number().doubleValue()) + "%)" + //
                        "\n" + format(de.totalDistanceWtCst) + " (" + //
                        DECIMAL.format(100 * de.totalDistanceWtCst.number().doubleValue() / de.totalDistance.number().doubleValue()) + "%)" + //
                        "\n" + //
                        "\n" + format(de.avgTripDistance));
        File img = new File(IMAGE_FOLDER, StackedDistanceChartImage.FILE_PNG);
        aRElement.getHTMLGenerator() //
                .insertImg(img.getPath(), StackedDistanceChartImage.WIDTH, StackedDistanceChartImage.HEIGHT);
        bodyElements.put(aRKey, aRElement);
        return bodyElements;
    }

    private static String format(Scalar scalar) {
        return DECIMAL.format(scalar.number().doubleValue()) + " " + QuantityUnit.of(scalar);
    }
}
