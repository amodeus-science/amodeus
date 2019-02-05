package ch.ethz.idsc.subare;

import ch.ethz.idsc.subare.plot.CompositionStack;
import ch.ethz.idsc.subare.plot.Histogram;
import ch.ethz.idsc.subare.plot.VisualRow;
import ch.ethz.idsc.subare.plot.VisualSet;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.pdf.RandomVariate;
import ch.ethz.idsc.tensor.pdf.UniformDistribution;
import junit.framework.TestCase;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.io.IOException;

public class PlotTest extends TestCase {

    public void test() throws IOException {
        Tensor domain = Tensors.fromString("{1, 2, 3, 4, 5}");
        Tensor values1 = RandomVariate.of(UniformDistribution.unit(), 5);
        Tensor values2 = RandomVariate.of(UniformDistribution.unit(), 5);

        VisualRow row1 = new VisualRow(domain, values1);
        VisualRow row2 = new VisualRow(domain, values2);

        VisualSet set = new VisualSet(row1, row2);

        set.setRowLabel(0, "row 1");
        set.setRowLabel(1, "row 2");

        JFreeChart chart1 = CompositionStack.of(set);
        File file1 = new File("compositionStack.png");
        ChartUtilities.saveChartAsPNG(file1, chart1, 200, 300);

        JFreeChart chart2 = Histogram.of(set);
        File file2 = new File("histogram.png");
        ChartUtilities.saveChartAsPNG(file2, chart2, 500, 300);
    }

}
