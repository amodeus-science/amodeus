/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Dimensions;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.pdf.BinCounts;

// TODO this class was marked deprecated but replacement has not been carried out yet
public enum HistogramPlot {
    ;

    /** Plots a histogram, using the following parameters:
     * 
     * @param values, values to use, e.g., {2,1,4,2,2,2,3,4,6,7}
     * @param binSize, binsize, e.g., RealScalar.of(0.5)
     * @param norm, true if the y-axis should be in %
     * @param directory, directory to save the chart in
     * @param filename, filename
     * @param diagramTitle, title of the diagram
     * @param yAxisLabel, label of x axis
     * @param xAxisLabel, label of y axis
     * @param imageWidth, width of the .png image
     * @param imageHeight, height of the .png image
     * @param colorDataIndexed, e.g., ColorDataAmodeus.indexed("097")
     * @param rangeYAxis, Range of the y Axis, e.g., Tensors.vector(0,100)
     * @param labels, optional labels for the data
     * @return
     * @throws Exception */
    public static File of(Tensor values, Scalar binSize, boolean norm, //
            File directory, String filename, String diagramTitle, //
            String yAxisLabel, String xAxisLabel, //
            int imageWidth, int imageHeight, ColorDataIndexed colorDataIndexed, Tensor rangeYAxis, //
            String... labels) //
            throws Exception {

        /** ensure that is a vector */ // must be a vector, values = {v1,v2,...,vn}
        List<Integer> dims = Dimensions.of(values);

        GlobalAssert.that(dims.size() == 1);

        /** compute bins */
        Scalar numValues = RationalScalar.of(values.length(), 1);
        Tensor bins = BinCounts.of(values, binSize);
        if (norm) // norm if needed
            bins = bins.map(s -> s.divide(numValues)).multiply(RealScalar.of(100));

        // if input Tensor is vector, convert to tensor
        List<Integer> dim = Dimensions.of(bins);
        if (dim.size() == 1) {
            bins = Tensors.of(bins);
        }
        GlobalAssert.that(bins.length() == labels.length || (dim.size() == 1 && labels.length == 0));
        GlobalAssert.that(Dimensions.of(bins).size() == 2); // bins must be a collection of vectors, i.e. dim == 2

        /** fill into data set */
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < bins.length(); ++i) {
            Tensor bin = bins.get(i);
            for (int j = 0; j < bin.length(); j++) {
                dataset.addValue(bin.Get(j).number().doubleValue(), labels.length > 0 ? labels[i] : "", //
                        StaticHelper.binName(binSize.number().doubleValue(), j));
            }
        }

        /** compute JFreeChart */
        JFreeChart chart = ChartFactory.createBarChart(diagramTitle, xAxisLabel, yAxisLabel, dataset, //
                PlotOrientation.VERTICAL, labels.length > 0 ? true : false, false, false);
        chart.getCategoryPlot().getDomainAxis().setLowerMargin(0.0);
        chart.getCategoryPlot().getDomainAxis().setUpperMargin(0.0);
        chart.getCategoryPlot().getDomainAxis().setCategoryMargin(0.0);
        chart.getCategoryPlot().getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        chart.getCategoryPlot().setRangeGridlinePaint(Color.lightGray);
        chart.getCategoryPlot().setRangeGridlinesVisible(true);
        chart.getCategoryPlot().setDomainGridlinePaint(Color.lightGray);
        chart.getCategoryPlot().setDomainGridlinesVisible(true);
        chart.getCategoryPlot().setDomainGridlinePosition(CategoryAnchor.START);

        /** fixes the y-axis */
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();
        numberAxis.setRange(rangeYAxis.Get(0).number().doubleValue(), rangeYAxis.Get(1).number().doubleValue());

        BarRenderer renderer = new BarRenderer();
        renderer.setDrawBarOutline(true);
        chart.getCategoryPlot().setRenderer(renderer);
        // Adapt colors & style
        for (int i = 0; i < dim.size(); i++) {
            chart.getCategoryPlot().getRenderer().setSeriesPaint(i, colorDataIndexed.getColor(i));
            chart.getCategoryPlot().getRenderer().setSeriesOutlinePaint(i, colorDataIndexed.getColor(i).darker());
            chart.getCategoryPlot().getRenderer().setSeriesOutlineStroke(i, new BasicStroke(1.0f));
        }
        return PlotSave.now(directory, filename, chart, imageWidth, imageHeight);
    }

}