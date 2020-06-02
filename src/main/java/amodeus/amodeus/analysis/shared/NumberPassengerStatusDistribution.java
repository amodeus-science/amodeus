/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.analysis.shared;

import java.io.File;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.jfree.chart.JFreeChart;

import amodeus.amodeus.analysis.AnalysisSummary;
import amodeus.amodeus.analysis.SaveUtils;
import amodeus.amodeus.analysis.element.AnalysisExport;
import amodeus.amodeus.analysis.element.AnalysisMeanFilter;
import amodeus.amodeus.analysis.element.NumberPassengersAnalysis;
import amodeus.amodeus.analysis.element.StatusDistributionElement;
import amodeus.amodeus.analysis.plot.AmodeusChartUtils;
import amodeus.amodeus.dispatcher.core.RoboTaxiStatus;
import amodeus.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Join;
import ch.ethz.idsc.tensor.alg.Reverse;
import ch.ethz.idsc.tensor.alg.Transpose;
import amodeus.tensor.fig.StackedTimedChart;
import amodeus.tensor.fig.VisualRow;
import amodeus.tensor.fig.VisualSet;
import ch.ethz.idsc.tensor.img.ColorDataGradients;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.opt.ScalarTensorFunction;
import ch.ethz.idsc.tensor.red.Total;

/** may */
public enum NumberPassengerStatusDistribution implements AnalysisExport {
    INSTANCE;

    public static final String TITLE = "statusDistributionNumPassengers";
    public static final String IMAGE_NAME = "statusDistributionNumPassengers.png";
    public static final ScalarTensorFunction COLOR_DATA_GRADIENT_DEFAULT = ColorDataGradients.SUNSET;
    public static final int WIDTH = 1000; /* Width of the image */
    public static final int HEIGHT = 750; /* Height of the image */

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {

        int numberVehicles = analysisSummary.getSimulationInformationElement().vehicleSize();
        NumberPassengersAnalysis nPA = analysisSummary.getNumberPassengersAnalysis();
        StatusDistributionElement sDE = analysisSummary.getStatusDistribution();

        /** create Time vector */
        Tensor time = nPA.getTime();
        GlobalAssert.that(time.equals(sDE.time));

        /** create values */
        Tensor statusesTensor = Transpose.of(sDE.statusTensor);
        Tensor notWithCustomer = Tensors.empty();
        notWithCustomer.append(statusesTensor.get(RoboTaxiStatus.DRIVETOCUSTOMER.ordinal()));
        notWithCustomer.append(statusesTensor.get(RoboTaxiStatus.REBALANCEDRIVE.ordinal()));
        notWithCustomer.append(statusesTensor.get(RoboTaxiStatus.STAY.ordinal()));
        notWithCustomer.append(statusesTensor.get(RoboTaxiStatus.OFFSERVICE.ordinal()));
        Tensor values = Reverse.of(Transpose.of(nPA.getPassengerDistribution()));
        Tensor withCustomer = values.extract(0, values.length() - 1);
        Tensor valuesComplet = Transpose.of(Join.of(withCustomer, notWithCustomer));

        /** check that all the timesteps contain all the Robo Taxis */
        Tensor testTensor = Total.of(Transpose.of(valuesComplet));
        testTensor.forEach(t -> GlobalAssert.that(t.Get().number().intValue() == numberVehicles));

        /** create status Labels */
        String[] statusLablesOnly = new String[] { //
                RoboTaxiStatus.DRIVETOCUSTOMER.description(), //
                RoboTaxiStatus.REBALANCEDRIVE.description(), //
                RoboTaxiStatus.STAY.description(), //
                RoboTaxiStatus.OFFSERVICE.description(), //
        };
        String[] passengerLabels = new String[withCustomer.length()];
        int numBins = withCustomer.length();
        IntStream.range(0, numBins).forEach(i -> passengerLabels[i] = numBins - i + " Passenger");

        String[] statusLabels = ArrayUtils.addAll(passengerLabels, statusLablesOnly);

        /** create Colors */
        NumberPassengerColorScheme nPCS = new NumberPassengerColorScheme(COLOR_DATA_GRADIENT_DEFAULT, colorDataIndexed);
        CustomColorDataCreator colorDataCreator = new CustomColorDataCreator();
        IntStream.range(0, numBins).forEach(i -> colorDataCreator.append(nPCS.of(RealScalar.of(numBins - i))));
        colorDataCreator.append(nPCS.of(RoboTaxiStatus.DRIVETOCUSTOMER));
        colorDataCreator.append(nPCS.of(RoboTaxiStatus.REBALANCEDRIVE));
        colorDataCreator.append(nPCS.of(RoboTaxiStatus.STAY));
        colorDataCreator.append(nPCS.of(RoboTaxiStatus.OFFSERVICE));
        // ColorDataIndexed colorScheme = colorDataCreator.getColorDataIndexed();

        /** create scaling factor */
        // double[] scale = new double[statusLabels.length];
        // Arrays.fill(scale, 1.0);

        /** Store Tensor */
        try {
            SaveUtils.saveFile(Join.of(valuesComplet), TITLE, relativeDirectory);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        /** plot image */
        VisualSet visualSet = new VisualSet(colorDataIndexed);
        for (int i = 0; i < statusLabels.length; ++i) {
            Tensor vals = valuesComplet.get(Tensor.ALL, i);
            vals = AnalysisMeanFilter.of(vals);
            VisualRow visualRow = visualSet.add(time, vals);
            visualRow.setLabel(statusLabels[i]);
        }

        visualSet.setPlotLabel("Number Passengers");
        visualSet.setAxesLabelY("RoboTaxis");

        JFreeChart chart = StackedTimedChart.of(visualSet);

        try {
            File fileChart = new File(relativeDirectory, IMAGE_NAME);
            AmodeusChartUtils.saveAsPNG(chart, fileChart.toString(), WIDTH, HEIGHT);
            GlobalAssert.that(fileChart.isFile());
            System.out.println("Exported " + IMAGE_NAME);
        } catch (Exception exception) {
            System.err.println("Plotting " + IMAGE_NAME + " failed");
            exception.printStackTrace();
        }
    }
}
