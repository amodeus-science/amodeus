/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import ch.ethz.idsc.subare.plot.Histogram;
import ch.ethz.idsc.subare.plot.ListPlot;
import ch.ethz.idsc.subare.plot.StackedHistogram;
import ch.ethz.idsc.subare.plot.StackedTablePlot;
import ch.ethz.idsc.subare.plot.StackedTimedChart;
import ch.ethz.idsc.subare.plot.TimedChart;
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

        /* amodeus specific */
        ChartFactory.setChartTheme(ChartTheme.STANDARD);

        {
            visualSet.setPlotLabel(StackedHistogram.class.getSimpleName());
            JFreeChart jFreeChart = StackedHistogram.of(visualSet);
            File file = new File(folder, StackedHistogram.class.getSimpleName() + ".png");
            ChartUtilities.saveChartAsPNG(file, jFreeChart, 200, 300);
        }

        {
            visualSet.setPlotLabel(Histogram.class.getSimpleName());
            JFreeChart jFreeChart = Histogram.of(visualSet);
            File file = new File(folder, Histogram.class.getSimpleName() + ".png");
            ChartUtilities.saveChartAsPNG(file, jFreeChart, 500, 300);
        }

        {
            visualSet.setPlotLabel(TimedChart.class.getSimpleName());
            JFreeChart jFreeChart = TimedChart.of(visualSet);
            File file = new File(folder, TimedChart.class.getSimpleName() + ".png");
            ChartUtilities.saveChartAsPNG(file, jFreeChart, 500, 300);
        }

        {
            visualSet.setPlotLabel(StackedTimedChart.class.getSimpleName());
            JFreeChart jFreeChart = StackedTimedChart.of(visualSet);
            File file = new File(folder, StackedTimedChart.class.getSimpleName() + ".png");
            ChartUtilities.saveChartAsPNG(file, jFreeChart, 500, 300);
        }

        {
            visualSet.setPlotLabel(ListPlot.class.getSimpleName());
            JFreeChart jFreeChart = ListPlot.of(visualSet);
            File file = new File(folder, ListPlot.class.getSimpleName() + ".png");
            ChartUtilities.saveChartAsPNG(file, jFreeChart, 500, 300);
        }

        {
            visualSet.setPlotLabel(StackedTablePlot.class.getSimpleName());
            JFreeChart jFreeChart = StackedTablePlot.of(visualSet);
            File file = new File(folder, StackedTablePlot.class.getSimpleName() + ".png");
            ChartUtilities.saveChartAsPNG(file, jFreeChart, 500, 300);
        }

    }

    public static void main(String[] args) throws IOException {
        demoPlots(HomeDirectory.Pictures("amodeus", "0"), false);
        demoPlots(HomeDirectory.Pictures("amodeus", "1"), true);
    }
}
