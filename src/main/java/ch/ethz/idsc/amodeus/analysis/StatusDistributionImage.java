/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;
import java.util.Arrays;

import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.StatusDistributionElement;
import ch.ethz.idsc.amodeus.analysis.plot.ColorScheme;
import ch.ethz.idsc.amodeus.analysis.plot.StackedTimeChart;

public class StatusDistributionImage implements AnalysisExport {
    public static final String FILENAME = "statusDistribution";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorScheme colorScheme) {
        String[] statusLabels = StaticHelper.descriptions();
        StatusDistributionElement st = analysisSummary.getStatusDistribution();

        Double[] scale = new Double[statusLabels.length];
        Arrays.fill(scale, 1.0);
        try {
            StackedTimeChart.of( //
                    relativeDirectory, //
                    FILENAME, //
                    "Status Distribution", //
                    StaticHelper.FILTER_ON, //
                    StaticHelper.FILTERSIZE, //
                    scale, //
                    statusLabels, //
                    "RoboTaxis", //
                    st.time, //
                    st.statusTensor, //
                    colorScheme);
        } catch (Exception e1) {
            System.err.println("The Modular status dist with Tensor was not carried out!!");
            e1.printStackTrace();
        }
    }
}
