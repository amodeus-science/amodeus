/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.shared;

import java.io.File;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.NumberPassengersAnalysis;
import ch.ethz.idsc.amodeus.analysis.element.StatusDistributionElement;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.subare.plot.VisualRow;
import ch.ethz.idsc.subare.plot.VisualSet;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Join;
import ch.ethz.idsc.tensor.alg.Reverse;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.img.ColorDataGradients;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.MeanFilter;
import ch.ethz.idsc.tensor.opt.ScalarTensorFunction;
import ch.ethz.idsc.tensor.red.Total;

public enum NumberPassengerStatusDistribution implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "statusDistributionNumPassengers";
    // TODO might be done dependent on the Secnario Options
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
            SaveUtils.saveFile(Join.of(valuesComplet), FILENAME, relativeDirectory);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /** plot image */
        VisualSet visualSet = new VisualSet(colorDataIndexed);
        for (int i = 0; i < statusLabels.length; ++i) {
            Tensor vals = Transpose.of(valuesComplet).get(i);
            vals = StaticHelper.FILTER_ON //
                    ? MeanFilter.of(vals, StaticHelper.FILTERSIZE)
                    : vals;
            VisualRow visualRow = visualSet.add(time, vals);
            visualRow.setLabel(statusLabels[i]);
        }

        visualSet.setPlotLabel("Number Passengers");
        visualSet.setAxesLabelY("RoboTaxis");

        JFreeChart chart = ch.ethz.idsc.subare.plot.StackedTimedChart.of(visualSet);

        try {
            File fileChart = new File(relativeDirectory, FILENAME + ".png");
            ChartUtilities.saveChartAsPNG(fileChart, chart, WIDTH, HEIGHT);
            GlobalAssert.that(fileChart.isFile());
            System.out.println("Exported " + FILENAME + ".png");
        } catch (Exception exception) {
            System.err.println("Plotting " + FILENAME + " failed");
            exception.printStackTrace();
        }
    }
}
