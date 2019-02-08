/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.File;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryLabelPositions;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.subare.plot.Histogram;
import ch.ethz.idsc.subare.plot.VisualSet;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Range;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.pdf.BinCounts;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.sca.Round;

/* package */ enum HistogramReportFigure {
    ;

    private static final int binNmbr = 30;
    private static final int width = 1000;
    private static final int height = 750;

    /** saves a histogram for the report with the values in @param vals {@link Tensor}
     * of format {v1,v2,v3,..,vN} with the maximum value @param maxVal in
     * the @param relativeDirectory and the name @param fileName
     * 
     * @param colorDataIndexed
     * @param title
     * @param xLabel */
    public static void of(Tensor vals, Scalar maxVal, //
            ColorDataIndexed colorDataIndexed, File relativeDirectory, String title, String xLabel, String fileName) {
        /** normally take integer valued bins */
        Scalar binNmbrScaling = RealScalar.of(1.0 / binNmbr);
        Scalar binSize = Round.of(maxVal.multiply(binNmbrScaling));
        /** for very low values, resolve in decimal steps */
        if (((Quantity) binSize).value().equals(RealScalar.ZERO))
            binSize = maxVal.multiply(binNmbrScaling);
        Tensor binCounter = BinCounts.of(vals, binSize);
        binCounter = binCounter.divide(RealScalar.of(vals.length())).multiply(RealScalar.of(100));

        VisualSet visualSet = new VisualSet();
        visualSet.add(Range.of(0, binCounter.length()).multiply(binSize), binCounter);
        // ---
        visualSet.setPlotLabel(title);
        visualSet.setRangeAxisLabel("% of requests");
        visualSet.setDomainAxisLabel(xLabel);
        visualSet.setColors(colorDataIndexed);

        final Scalar size = binSize;
        JFreeChart chart = Histogram.of(visualSet, s -> "[" + s.number() + " , " + s.add(size).number() + ")");
        chart.getCategoryPlot().getDomainAxis().setLowerMargin(0.0);
        chart.getCategoryPlot().getDomainAxis().setUpperMargin(0.0);
        chart.getCategoryPlot().getDomainAxis().setCategoryMargin(0.0);
        chart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        chart.getCategoryPlot().setDomainGridlinePosition(CategoryAnchor.START);

        try {
            File fileChart = new File(relativeDirectory, fileName + ".png");
            ChartUtilities.saveChartAsPNG(fileChart, chart, width, height);
            GlobalAssert.that(fileChart.isFile());
            System.out.println("Exported " + fileName + ".png");
        } catch (Exception e) {
            System.err.println("Plotting " + fileName + " failed");
            e.printStackTrace();
        }
    }
}
