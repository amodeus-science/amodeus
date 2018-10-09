/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;
import java.util.Arrays;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.plot.StackedTimeChart;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum StatusDistributionImage implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "statusDistribution";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
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
                    colorDataIndexed);
        } catch (Exception e1) {
            System.err.println("The Modular status dist with Tensor was not carried out!!");
            e1.printStackTrace();
        }
    }
}
