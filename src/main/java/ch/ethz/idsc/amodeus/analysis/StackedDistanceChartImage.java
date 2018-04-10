/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;
import ch.ethz.idsc.amodeus.analysis.plot.ColorScheme;
import ch.ethz.idsc.amodeus.analysis.plot.CompositionStack;

public class StackedDistanceChartImage implements AnalysisExport {
    public static final String FILENAME = "stackedDistance";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorScheme colorScheme) {
        DistanceElement de = analysisSummary.getDistanceElement();
        String[] labels = { "With Customer", "Pickup", "Rebalancing" };
        double[] values = new double[] { //
                de.totalDistanceWtCst / de.totalDistance, //
                de.totalDistancePicku / de.totalDistance, //
                de.totalDistanceRebal / de.totalDistance };
        try {
            CompositionStack.of( //
                    relativeDirectory, //
                    FILENAME, //
                    "Total Distance Distribution", //
                    values, //
                    labels, //
                    colorScheme);
        } catch (Exception e) {
            System.err.println("The Stacked Distance Plot was not successfull");
            e.printStackTrace();
        }
    }
}
