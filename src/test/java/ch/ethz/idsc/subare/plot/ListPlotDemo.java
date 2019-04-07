/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.plot;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import ch.ethz.idsc.amodeus.analysis.plot.ChartTheme;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.img.ColorDataLists;
import ch.ethz.idsc.tensor.io.HomeDirectory;
import ch.ethz.idsc.tensor.pdf.RandomVariate;
import ch.ethz.idsc.tensor.pdf.UniformDistribution;

/* package */ enum ListPlotDemo {
    ;
    public static void main(String[] args) throws IOException {
        Tensor values1 = RandomVariate.of(UniformDistribution.unit(), 5);
        Tensor values2 = RandomVariate.of(UniformDistribution.unit(), 15);
        Tensor values3 = RandomVariate.of(UniformDistribution.unit(), 10);

        VisualSet visualSet = new VisualSet(ColorDataLists._250.cyclic());

        Tensor domain1 = RandomVariate.of(UniformDistribution.unit(), values1.length());
        visualSet.add(domain1, values1);

        Tensor domain2 = RandomVariate.of(UniformDistribution.unit(), values2.length());
        visualSet.add(domain2, values2);

        Tensor domain3 = RandomVariate.of(UniformDistribution.unit(), values3.length());
        visualSet.add(domain3, values3);

        Tensor domain4 = Tensors.vector(1, 3, 2, 5, 4).multiply(RealScalar.of(.2));
        visualSet.add(domain4, domain4);

        /* amodeus specific */
        ChartFactory.setChartTheme(ChartTheme.STANDARD);

        {
            JFreeChart jFreeChart = ListPlot.of(visualSet);
            File file = HomeDirectory.Pictures(ListPlot.class.getSimpleName() + ".png");
            ChartUtilities.saveChartAsPNG(file, jFreeChart, 500, 300);
        }

    }

}
