/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.plot.TimeChart;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum OccupancyDistanceRatiosImage implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "occAndDistRatios";
    private static final String[] RATIOS_LABELS = new String[] { "occupancy ratio", "distance ratio" };

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        DistanceElement de = analysisSummary.getDistanceElement();
        double[] scaleratios = new double[] { 1.0, 1.0 };
        try {
            TimeChart.of( //
                    relativeDirectory, //
                    FILENAME, //
                    "Occupancy and Distance Ratios", //
                    StaticHelper.FILTER_ON, //
                    StaticHelper.FILTERSIZE, //
                    scaleratios, //
                    RATIOS_LABELS, //
                    "Time", //
                    "occupancy / distance ratio", //
                    de.time, //
                    de.ratios, //
                    new Double[] { 0.0, 1.0 }, colorDataIndexed);
        } catch (Exception e1) {
            System.err.println("The Modular Ratios Plot was not sucessful!!");
            e1.printStackTrace();
        }

    }

}
