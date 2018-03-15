/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;
import java.util.Arrays;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.DistanceElement;
import ch.ethz.idsc.amodeus.analysis.plot.StackedTimeChart;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Transpose;

public class DistanceDistributionOverDayImage implements AnalysisExport {
    public static final String FILENAME = "distanceDistribution";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory) {
        DistanceElement de = analysisSummary.getDistanceElement();
        String[] diagramLables = Arrays.copyOf(StaticHelper.descriptions(), 3);
        Double[] scale = new Double[diagramLables.length];
        Arrays.fill(scale, 1.0);
        scale[0] = -1.0;
        Tensor distances = Transpose.of(Transpose.of(de.distancesOverDay).extract(1, 4));
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
                    distances);
        } catch (Exception e) {
            System.err.println("modularDistanceDistribution plot was unsucessfull");
            e.printStackTrace();
        }

    }

}
