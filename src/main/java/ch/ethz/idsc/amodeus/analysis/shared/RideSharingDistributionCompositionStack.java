/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.shared;

import java.io.File;
import java.util.stream.IntStream;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.NumberPassengersAnalysis;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.subare.plot.StackedHistogram;
import ch.ethz.idsc.subare.plot.VisualSet;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.red.Total;

public enum RideSharingDistributionCompositionStack implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "SharedDistributionTotal";
    public static final int WIDTH = 700; /* Width of the image */
    public static final int HEIGHT = 125; /* Height of the image */
    // private double requestSharedRate = -1;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        NumberPassengersAnalysis numberPassengersAnalysis = analysisSummary.getNumberPassengersAnalysis();

        /** Get Values */
        Tensor sharedDistribution = numberPassengersAnalysis.getSharedOthersDistribution();
        CustomColorDataCreator colorDataCreator = new CustomColorDataCreator();
        /** create Colors */
        {
            NumberPassengerColorScheme numberPassengerColorScheme = new NumberPassengerColorScheme( //
                    NumberPassengerStatusDistribution.COLOR_DATA_GRADIENT_DEFAULT, //
                    colorDataIndexed);
            IntStream.range(1, sharedDistribution.length() + 1) //
                    .forEach(i -> colorDataCreator.append(numberPassengerColorScheme.of(RealScalar.of(i))));
        }
        // ---
        VisualSet visualSet = new VisualSet(colorDataCreator.getColorDataIndexed());
        Scalar totalNumberPassengers = Total.of(sharedDistribution).Get();
        sharedDistribution.forEach(s -> visualSet.add( //
                Tensors.matrix(new Scalar[][] { //
                        { RealScalar.ONE, (Scalar) s.divide(totalNumberPassengers) } })) //
        );
        for (int i = 0; i < visualSet.visualRows().size(); ++i)
            visualSet.getVisualRow(i).setLabel((i + 1) + " Passengers");
        visualSet.setPlotLabel("Ride Sharing Distribution, fraction of Requests");

        JFreeChart chart = StackedHistogram.of(visualSet);
        chart.getCategoryPlot().setOrientation(PlotOrientation.HORIZONTAL);
        chart.getCategoryPlot().getRangeAxis().setRange(0, 1.0);

        try {
            File fileChart = new File(relativeDirectory, FILENAME + ".png");
            ChartUtilities.saveChartAsPNG(fileChart, chart, WIDTH, HEIGHT);
            GlobalAssert.that(fileChart.isFile());
            System.out.println("Exported " + FILENAME + ".png");
        } catch (Exception e) {
            System.err.println("Plotting " + FILENAME + " failed");
            e.printStackTrace();
        }
    }

}
