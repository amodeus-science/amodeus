package ch.ethz.idsc.subare;

import ch.ethz.idsc.amodeus.analysis.plot.ChartTheme;
import ch.ethz.idsc.subare.plot.*;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.img.ColorDataLists;
import ch.ethz.idsc.tensor.pdf.RandomVariate;
import ch.ethz.idsc.tensor.pdf.UniformDistribution;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.io.IOException;

public class PlotDemo {

    public static void main(String[] args) throws IOException {
                Tensor domain = Tensors.fromString("{1, 2, 3, 4, 5}");
        Tensor values1 = RandomVariate.of(UniformDistribution.unit(), 5);
        Tensor values2 = RandomVariate.of(UniformDistribution.unit(), 5);

        VisualRow row1 = new VisualRow(domain, values1);
        VisualRow row2 = new VisualRow(domain, values2);

        VisualSet visualSet = new VisualSet(row1, row2);

        visualSet.setRowLabel(0, "row 1");
        visualSet.setRowLabel(1, "row 2");
        visualSet.setPlotLabel("title");
        visualSet.setDomainAxisLabel("x axis");

        visualSet.setColors(ColorDataLists._097.cyclic());

        ChartFactory.setChartTheme(/* amodeus specific */ ChartTheme.STANDARD.getChartTheme(false));

        JFreeChart chart1 = CompositionStack.of(visualSet);
        File file1 = new File("compositionStack.png");
        ChartUtilities.saveChartAsPNG(file1, chart1, 200, 300);

        JFreeChart chart2 = Histogram.of(visualSet);
        File file2 = new File("histogram.png");
        ChartUtilities.saveChartAsPNG(file2, chart2, 500, 300);

        JFreeChart chart3 = TimeChart.of(visualSet);
        File file3 = new File("time.png");
        ChartUtilities.saveChartAsPNG(file3, chart3, 500, 300);

        JFreeChart chart4 = StackedTimeChart.of(visualSet);
        File file4 = new File("stackedTime.png");
        ChartUtilities.saveChartAsPNG(file4, chart4, 500, 300);

        JFreeChart chart5 = ListPlot.of(visualSet);
        File file5 = new File("line.png");
        ChartUtilities.saveChartAsPNG(file5, chart5, 500, 300);

        JFreeChart chart6 = ListPlot.of(visualSet,true);
        File file6 = new File("stackedLine.png");
        ChartUtilities.saveChartAsPNG(file6, chart6, 500, 300);
    }

}
