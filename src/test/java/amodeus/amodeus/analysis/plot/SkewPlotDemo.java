/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.plot;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;

import amodeus.tensor.fig.Histogram;
import amodeus.tensor.fig.ListPlot;
import amodeus.tensor.fig.StackedHistogram;
import amodeus.tensor.fig.StackedTablePlot;
import amodeus.tensor.fig.StackedTimedChart;
import amodeus.tensor.fig.TimedChart;
import amodeus.tensor.fig.VisualRow;
import amodeus.tensor.fig.VisualSet;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Range;
import ch.ethz.idsc.tensor.ext.HomeDirectory;
import ch.ethz.idsc.tensor.img.ColorDataLists;
import ch.ethz.idsc.tensor.pdf.RandomVariate;
import ch.ethz.idsc.tensor.pdf.UniformDistribution;

enum SkewPlotDemo {
    ;
    static void demoPlots(File folder, boolean labels) throws IOException {
        folder.mkdirs();

        Tensor values1 = RandomVariate.of(UniformDistribution.unit(), 5);
        Tensor values2 = RandomVariate.of(UniformDistribution.unit(), 15);
        Tensor values3 = RandomVariate.of(UniformDistribution.unit(), 10);

        VisualSet visualSet = new VisualSet(ColorDataLists._250.cyclic());

        VisualRow row0 = visualSet.add(Range.of(0, values1.length()), values1);
        // VisualRow row1 =
        visualSet.add(Range.of(0, values2.length()), values2);
        VisualRow row2 = visualSet.add(Range.of(3, 3 + values3.length()), values3);

        if (labels) {
            row0.setLabel("row 0");
            // row2.setLabel("row 2");
            row2.setLabel("row 2");

            visualSet.setAxesLabelX("x axis");
            visualSet.setAxesLabelY("y axis");
        }

        /* amodeus specific */
        ChartFactory.setChartTheme(ChartTheme.STANDARD);

        {
            visualSet.setPlotLabel(StackedHistogram.class.getSimpleName());
            JFreeChart jFreeChart = StackedHistogram.of(visualSet);
            File file = new File(folder, StackedHistogram.class.getSimpleName() + ".png");
            AmodeusChartUtils.saveAsPNG(jFreeChart, file.toString(), 500, 300);
        }

        {
            visualSet.setPlotLabel(Histogram.class.getSimpleName());
            JFreeChart jFreeChart = Histogram.of(visualSet);
            File file = new File(folder, Histogram.class.getSimpleName() + ".png");
            AmodeusChartUtils.saveAsPNG(jFreeChart, file.toString(), 500, 300);
        }

        {
            visualSet.setPlotLabel(TimedChart.class.getSimpleName());
            JFreeChart jFreeChart = TimedChart.of(visualSet);
            File file = new File(folder, TimedChart.class.getSimpleName() + ".png");
            AmodeusChartUtils.saveAsPNG(jFreeChart, file.toString(), 500, 300);
        }

        {
            visualSet.setPlotLabel(StackedTimedChart.class.getSimpleName());
            JFreeChart jFreeChart = StackedTimedChart.of(visualSet);
            File file = new File(folder, StackedTimedChart.class.getSimpleName() + ".png");
            AmodeusChartUtils.saveAsPNG(jFreeChart, file.toString(), 500, 300);
        }

        {
            visualSet.setPlotLabel(ListPlot.class.getSimpleName());
            JFreeChart jFreeChart = ListPlot.of(visualSet);
            File file = new File(folder, ListPlot.class.getSimpleName() + ".png");
            AmodeusChartUtils.saveAsPNG(jFreeChart, file.toString(), 500, 300);
        }

        {
            visualSet.setPlotLabel(StackedTablePlot.class.getSimpleName());
            JFreeChart jFreeChart = StackedTablePlot.of(visualSet);
            File file = new File(folder, StackedTablePlot.class.getSimpleName() + ".png");
            AmodeusChartUtils.saveAsPNG(jFreeChart, file.toString(), 500, 300);
        }

    }

    public static void main(String[] args) throws IOException {
        demoPlots(HomeDirectory.Pictures("amodeus", "2"), false);
        demoPlots(HomeDirectory.Pictures("amodeus", "3"), true);
    }
}
