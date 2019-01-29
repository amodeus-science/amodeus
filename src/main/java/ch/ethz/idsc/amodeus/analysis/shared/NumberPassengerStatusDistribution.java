/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.shared;

import java.io.File;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.NumberPassengersAnalysis;
import ch.ethz.idsc.amodeus.analysis.element.StatusDistributionElement;
import ch.ethz.idsc.amodeus.analysis.plot.StackedTimeChart;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Join;
import ch.ethz.idsc.tensor.alg.Reverse;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.img.ColorDataGradients;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.opt.ScalarTensorFunction;
import ch.ethz.idsc.tensor.red.Total;

public enum NumberPassengerStatusDistribution implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "statusDistributionNumPassengers";
    // TODO might be done dependent on the Secnario Options
    public static final ScalarTensorFunction COLOR_DATA_GRADIENT_DEFAULT = ColorDataGradients.SUNSET;

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
        notWithCustomer.append(statusesTensor.get(RoboTaxiStatus.PARKING.ordinal()));
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
                RoboTaxiStatus.PARKING.description(), //
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
        colorDataCreator.append(nPCS.of(RoboTaxiStatus.PARKING));
        colorDataCreator.append(nPCS.of(RoboTaxiStatus.STAY));
        colorDataCreator.append(nPCS.of(RoboTaxiStatus.OFFSERVICE));
        ColorDataIndexed colorScheme = colorDataCreator.getColorDataIndexed();

        /** create scaling factor */
        Double[] scale = new Double[statusLabels.length];
        Arrays.fill(scale, 1.0);

        /** plot image */
        try {
            StackedTimeChart.of( //
                    relativeDirectory, //
                    FILENAME, //
                    "Number Passengers", //
                    StaticHelper.FILTER_ON, //
                    StaticHelper.FILTERSIZE, //
                    scale, //
                    statusLabels, //
                    "RoboTaxis", //
                    time, //
                    valuesComplet, //
                    colorScheme);
        } catch (Exception e1) {
            System.err.println("The Modular number Passenger Tensor was not carried out!!");
            e1.printStackTrace();
        }

        /** Store Tensor */
        try {
            SaveUtils.saveFile(Join.of(valuesComplet), FILENAME, relativeDirectory);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
