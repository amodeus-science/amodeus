/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.element;

import java.io.File;

import org.jfree.chart.JFreeChart;

import amodeus.amodeus.analysis.AnalysisSummary;
import amodeus.amodeus.analysis.plot.AmodeusChartUtils;
import amodeus.amodeus.util.math.GlobalAssert;
import amodeus.tensor.fig.VisualRow;
import amodeus.tensor.fig.VisualSet;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public enum StatusDistributionImage implements AnalysisExport {
    INSTANCE;

    public static final String FILE_PNG = "statusDistribution.png";
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 750;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        String[] statusLabels = StaticHelper.descriptions();
        StatusDistributionElement st = analysisSummary.getStatusDistribution();

        VisualSet visualSet = new VisualSet(colorDataIndexed);
        for (int i = 0; i < statusLabels.length; i++) {
            Tensor values = st.statusTensor.get(Tensor.ALL, i);
            values = AnalysisMeanFilter.of(values);
            VisualRow visualRow = visualSet.add(st.time, values);
            visualRow.setLabel(statusLabels[i]);
        }

        visualSet.setPlotLabel("Status Distribution");
        visualSet.setAxesLabelY("RoboTaxis");

        JFreeChart chart = amodeus.tensor.fig.StackedTimedChart.of(visualSet);

        try {
            File fileChart = new File(relativeDirectory, FILE_PNG);
            AmodeusChartUtils.saveAsPNG(chart, fileChart.toString(), WIDTH, HEIGHT);
            GlobalAssert.that(fileChart.isFile());
            System.out.println("Exported " + FILE_PNG);
        } catch (Exception e) {
            System.err.println("Plotting " + FILE_PNG + " failed");
            e.printStackTrace();
        }
    }
}
