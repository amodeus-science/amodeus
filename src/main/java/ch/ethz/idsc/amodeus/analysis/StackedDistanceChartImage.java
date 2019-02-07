/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;
import ch.ethz.idsc.amodeus.analysis.plot.CompositionStack;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum StackedDistanceChartImage implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "stackedDistance";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        DistanceElement de = analysisSummary.getDistanceElement();
        String[] labels = { "With Customer", "Pickup", "Rebalancing" , "Parking"};
        double[] values = new double[] { //
                de.totalDistanceWtCst / de.totalDistance, //
                de.totalDistancePicku / de.totalDistance, //
                de.totalDistanceRebal / de.totalDistance, //
                de.totalDistanceParki / de.totalDistance};
        try {
            CompositionStack.of( //
                    relativeDirectory, //
                    FILENAME, //
                    "Total Distance Distribution", //
                    values, //
                    labels, //
                    colorDataIndexed);
        } catch (Exception e) {
            System.err.println("The Stacked Distance Plot was not successfull");
            e.printStackTrace();
        }
    }
}
