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
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Dimensions;

public enum HistogramPlot {
    ;

    /** @param bins vector or tensor containing vectors with the respective histogram values
     * @param directory where the plot will be saved
     * @param diagramTitle on top of the diagram and also used as name for the .png file
     * @param binSize used beforehand to produce bins
     * @param axisLabelY
     * @param axisLabelX
     * @param imageWidth
     * @param imageHeight
     * @param labels for the respective histogram columns
     * @return .png image file of the plot
     * @throws Exception */
    public static File of(Tensor bins, File directory, String filename, String diagramTitle, //
            double binSize, String axisLabelY, String axisLabelX, //
            int imageWidth, int imageHeight, ColorScheme colorScheme, String... labels) throws Exception {

        // if input Tensor is vector, convert to tensor
        List<Integer> dim = Dimensions.of(bins);
        if (dim.size() == 1) {
            bins = Tensors.of(bins);
        }
        GlobalAssert.that(bins.length() == labels.length || (dim.size() == 1 && labels.length == 0));
        GlobalAssert.that(Dimensions.of(bins).size() == 2); // bins must be a collection of vectors, i.e. dim == 2

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < bins.length(); ++i) {
            Tensor bin = bins.get(i);
            for (int j = 0; j < bin.length(); j++) {
                dataset.addValue(bin.Get(j).number().doubleValue(), labels.length > 0 ? labels[i] : "", binName(binSize, j));
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(diagramTitle, axisLabelX, axisLabelY, dataset, //
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

        BarRenderer renderer = new BarRenderer();
        renderer.setDrawBarOutline(true);

        chart.getCategoryPlot().setRenderer(renderer);

        // TODO Does not need to be set anymore since the settings are centralized in ChartTheme for all Chart types
        // chart.getPlot().setBackgroundPaint(DiagramSettings.COLOR_BACKGROUND_PAINT);
        // chart.getCategoryPlot().setRangeGridlinePaint(DiagramSettings.COLOR_GRIDLINE_PAINT);
        // chart.getCategoryPlot().getDomainAxis().setTickLabelFont(DiagramSettings.FONT_TICK);
        // chart.getCategoryPlot().getDomainAxis().setLabelFont(DiagramSettings.FONT_AXIS);
        // chart.getCategoryPlot().getRangeAxis().setTickLabelFont(DiagramSettings.FONT_TICK);
        // chart.getCategoryPlot().getRangeAxis().setLabelFont(DiagramSettings.FONT_AXIS);
        // chart.getTitle().setFont(DiagramSettings.FONT_TITLE);

        // Adapt colors & style
        for (int i = 0; i < dim.size(); i++) {
            chart.getCategoryPlot().getRenderer().setSeriesPaint(i, colorScheme.of(i));
            chart.getCategoryPlot().getRenderer().setSeriesOutlinePaint(i, colorScheme.of(i).darker());
            chart.getCategoryPlot().getRenderer().setSeriesOutlineStroke(i, new BasicStroke(1.0f));
        }

        return StaticHelper.savePlot(directory, filename, chart, imageWidth, imageHeight);
    }

    private static String binName(double binSize, int i) {
        if (isInteger(binSize)) {
            int binSizeInt = (int) binSize;
            return "[" + i * binSizeInt + " , " + (i + 1) * binSizeInt + ")";
        }
        return "[" + i * binSize + " , " + (i + 1) * binSize + ")";

    }

    public static boolean isInteger(double d) {
        return d == Math.floor(d) && Double.isFinite(d);
    }

    public static String fileTitle(String diagramTitle) {
        return diagramTitle.replaceAll("\\s+", "");
    }

}
