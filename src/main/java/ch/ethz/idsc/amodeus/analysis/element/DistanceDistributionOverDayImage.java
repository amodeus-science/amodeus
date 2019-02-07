/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;
import java.util.Arrays;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.plot.StackedTimeChart;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum DistanceDistributionOverDayImage implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "distanceDistribution";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        DistanceElement de = analysisSummary.getDistanceElement();
        String[] diagramLables = Arrays.copyOf(StaticHelper.descriptions(), 4);
        Double[] scale = new Double[diagramLables.length];
        Arrays.fill(scale, 1.0);
        scale[0] = -1.0;
        Tensor distances = Transpose.of(Transpose.of(de.distancesOverDay).extract(1, 5));
        try {
            StackedTimeChart.of( //
                    relativeDirectory, //
                    FILENAME, //
                    "Distance Distribution over Day", //
                    StaticHelper.FILTER_ON, //
                    StaticHelper.FILTERSIZE, //
                    scale, //
                    diagramLables, //
                    "Distance [km]", //
                    de.time, //
                    distances, //
                    colorDataIndexed);
        } catch (Exception e) {
            System.err.println("modularDistanceDistribution plot was unsucessfull");
            e.printStackTrace();
        }

    }

}
