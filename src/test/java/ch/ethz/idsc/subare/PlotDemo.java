/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import ch.ethz.idsc.amodeus.analysis.plot.ChartTheme;
import ch.ethz.idsc.subare.plot.CompositionStack;
import ch.ethz.idsc.subare.plot.Histogram;
import ch.ethz.idsc.subare.plot.ListPlot;
import ch.ethz.idsc.subare.plot.StackedTimeChart;
import ch.ethz.idsc.subare.plot.TimeChart;
import ch.ethz.idsc.subare.plot.VisualRow;
import ch.ethz.idsc.subare.plot.VisualSet;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Range;
import ch.ethz.idsc.tensor.img.ColorDataLists;
import ch.ethz.idsc.tensor.io.HomeDirectory;
import ch.ethz.idsc.tensor.pdf.RandomVariate;
import ch.ethz.idsc.tensor.pdf.UniformDistribution;

enum PlotDemo {
    ;
    static void demoPlots(File folder, boolean labels) throws IOException {
        folder.mkdirs();

        Tensor domain = Range.of(0, 20);
        Tensor values1 = RandomVariate.of(UniformDistribution.unit(), domain.length());
        Tensor values2 = RandomVariate.of(UniformDistribution.unit(), domain.length());
        Tensor values3 = RandomVariate.of(UniformDistribution.unit(), domain.length());

        VisualSet visualSet = new VisualSet(ColorDataLists._001.cyclic());

        VisualRow row0 = visualSet.add(domain, values1);
        // VisualRow row1 =
        visualSet.add(domain, values2);
        VisualRow row2 = visualSet.add(domain, values3);

        if (labels) {
            row0.setLabel("row 0");
            // row2.setLabel("row 2");
            row2.setLabel("row 2");
            visualSet.setPlotLabel("title");
            visualSet.setDomainAxisLabel("x axis");
            visualSet.setRangeAxisLabel("y axis");
        }

        ChartFactory.setChartTheme(/* amodeus specific */ ChartTheme.STANDARD.getChartTheme(false));

        {
            JFreeChart jFreeChart = CompositionStack.of(visualSet);
            File file1 = new File(folder, CompositionStack.class.getSimpleName() + ".png");
            ChartUtilities.saveChartAsPNG(file1, jFreeChart, 200, 300);
        }

        {
            JFreeChart jFreeChart = Histogram.of(visualSet);
            File file2 = new File(folder, Histogram.class.getSimpleName() + ".png");
            ChartUtilities.saveChartAsPNG(file2, jFreeChart, 500, 300);
        }

        {
            JFreeChart jFreeChart = TimeChart.of(visualSet);
            File file3 = new File(folder, TimeChart.class.getSimpleName() + ".png");
            ChartUtilities.saveChartAsPNG(file3, jFreeChart, 500, 300);
        }

        {
            JFreeChart jFreeChart = StackedTimeChart.of(visualSet);
            File file4 = new File(folder, StackedTimeChart.class.getSimpleName() + ".png");
            ChartUtilities.saveChartAsPNG(file4, jFreeChart, 500, 300);
        }

        {
            JFreeChart jFreeChart = ListPlot.of(visualSet);
            File file5 = new File(folder, ListPlot.class.getSimpleName() + ".png");
            ChartUtilities.saveChartAsPNG(file5, jFreeChart, 500, 300);
        }

        {
            JFreeChart jFreeChart = ListPlot.of(visualSet, true);
            File file6 = new File(folder, "stacked.png");
            ChartUtilities.saveChartAsPNG(file6, jFreeChart, 500, 300);
        }

    }

    public static void main(String[] args) throws IOException {
        demoPlots(HomeDirectory.Pictures("amodeus", "0"), false);
        demoPlots(HomeDirectory.Pictures("amodeus", "1"), true);
    }
}
