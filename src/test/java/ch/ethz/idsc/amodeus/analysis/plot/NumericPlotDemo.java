/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.io.File;
import java.io.IOException;

import ch.ethz.idsc.tensor.RealScalar;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import ch.ethz.idsc.subare.plot.Histogram;
import ch.ethz.idsc.subare.plot.ListPlot;
import ch.ethz.idsc.subare.plot.StackedHistogram;
import ch.ethz.idsc.subare.plot.StackedTimeChart;
import ch.ethz.idsc.subare.plot.TimeChart;
import ch.ethz.idsc.subare.plot.VisualRow;
import ch.ethz.idsc.subare.plot.VisualSet;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Sort;
import ch.ethz.idsc.tensor.img.ColorDataLists;
import ch.ethz.idsc.tensor.io.HomeDirectory;
import ch.ethz.idsc.tensor.pdf.RandomVariate;
import ch.ethz.idsc.tensor.pdf.UniformDistribution;

enum NumericPlotDemo {
    ;
    static void demoPlots(File folder, boolean labels) throws IOException {
        folder.mkdirs();

        Tensor values1 = RandomVariate.of(UniformDistribution.unit(), 5);
        Tensor values2 = RandomVariate.of(UniformDistribution.unit(), 15);
        Tensor values3 = RandomVariate.of(UniformDistribution.unit(), 10);

        VisualSet visualSet = new VisualSet(ColorDataLists._250.cyclic());

        Tensor domain1 = Sort.of(RandomVariate.of(UniformDistribution.unit(), values1.length())) //
                .multiply(RealScalar.of(10));
        VisualRow row0 = visualSet.add(domain1, values1);

        Tensor domain2 = Sort.of(RandomVariate.of(UniformDistribution.unit(), values2.length())) //
                .multiply(RealScalar.of(10));
        visualSet.add(domain2, values2);

        Tensor domain3 = Sort.of(RandomVariate.of(UniformDistribution.unit(), values3.length())) //
                .multiply(RealScalar.of(10));
        VisualRow row2 = visualSet.add(domain3, values3);

        if (labels) {
            row0.setLabel("row 0");
            // row2.setLabel("row 2");
            row2.setLabel("row 2");
            visualSet.setPlotLabel("title");
            visualSet.setDomainAxisLabel("x axis");
            visualSet.setRangeAxisLabel("y axis");
        }

        /* amodeus specific */
        ChartFactory.setChartTheme(ChartTheme.STANDARD);

        {
            JFreeChart jFreeChart = StackedHistogram.of(visualSet);
            File file = new File(folder, StackedHistogram.class.getSimpleName() + ".png");
            ChartUtilities.saveChartAsPNG(file, jFreeChart, 500, 300);
        }

        {
            JFreeChart jFreeChart = Histogram.of(visualSet);
            File file = new File(folder, Histogram.class.getSimpleName() + ".png");
            ChartUtilities.saveChartAsPNG(file, jFreeChart, 500, 300);
        }

        {
            JFreeChart jFreeChart = TimeChart.of(visualSet);
            File file = new File(folder, TimeChart.class.getSimpleName() + ".png");
            ChartUtilities.saveChartAsPNG(file, jFreeChart, 500, 300);
        }

        {
            JFreeChart jFreeChart = StackedTimeChart.of(visualSet);
            File file = new File(folder, StackedTimeChart.class.getSimpleName() + ".png");
            ChartUtilities.saveChartAsPNG(file, jFreeChart, 500, 300);
        }

        {
            JFreeChart jFreeChart = ListPlot.of(visualSet);
            File file = new File(folder, ListPlot.class.getSimpleName() + ".png");
            ChartUtilities.saveChartAsPNG(file, jFreeChart, 500, 300);
        }

    }

    public static void main(String[] args) throws IOException {
        demoPlots(HomeDirectory.Pictures("amodeus", "4"), false);
        demoPlots(HomeDirectory.Pictures("amodeus", "5"), true);
    }
}
