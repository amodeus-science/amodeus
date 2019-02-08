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

        Tensor domain = Range.of(0, 10);
        Tensor values1 = RandomVariate.of(UniformDistribution.unit(), domain.length());
        Tensor values2 = RandomVariate.of(UniformDistribution.unit(), domain.length());

        VisualSet visualSet = new VisualSet();
        VisualRow row1 = visualSet.add(domain, values1);
        VisualRow row2 = visualSet.add(domain, values2);

        visualSet.setColors(ColorDataLists._097.cyclic());
        
        if (labels) {
            row1.setLabel("row 1");
            row2.setLabel("row 2");
            visualSet.setPlotLabel("title");
            visualSet.setDomainAxisLabel("x axis");
            visualSet.setRangeAxisLabel("y axis");
        }

        ChartFactory.setChartTheme(/* amodeus specific */ ChartTheme.STANDARD.getChartTheme(false));

        JFreeChart chart1 = CompositionStack.of(visualSet);
        File file1 = new File(folder, "compositionStack.png");
        ChartUtilities.saveChartAsPNG(file1, chart1, 200, 300);

        JFreeChart chart2 = Histogram.of(visualSet);
        File file2 = new File(folder, "histogram.png");
        ChartUtilities.saveChartAsPNG(file2, chart2, 500, 300);

        JFreeChart chart3 = TimeChart.of(visualSet);
        File file3 = new File(folder, "time.png");
        ChartUtilities.saveChartAsPNG(file3, chart3, 500, 300);

        JFreeChart chart4 = StackedTimeChart.of(visualSet);
        File file4 = new File(folder, "stackedTime.png");
        ChartUtilities.saveChartAsPNG(file4, chart4, 500, 300);

        JFreeChart chart5 = ListPlot.of(visualSet);
        File file5 = new File(folder, "line.png");
        ChartUtilities.saveChartAsPNG(file5, chart5, 500, 300);

        JFreeChart chart6 = ListPlot.of(visualSet, true);
        File file6 = new File(folder, "stackedLine.png");
        ChartUtilities.saveChartAsPNG(file6, chart6, 500, 300);

    }

    public static void main(String[] args) throws IOException {
        demoPlots(HomeDirectory.Pictures("amodeus", "0"), false);
        demoPlots(HomeDirectory.Pictures("amodeus", "1"), true);
    }
}
