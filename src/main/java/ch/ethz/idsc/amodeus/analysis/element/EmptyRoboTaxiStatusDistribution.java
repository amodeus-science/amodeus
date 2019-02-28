package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.plot.StackedTimeChart;
import ch.ethz.idsc.amodeus.analysis.shared.CustomColorDataCreator;
import ch.ethz.idsc.amodeus.analysis.shared.NumberPassengerColorScheme;
import ch.ethz.idsc.amodeus.analysis.element.SaveUtils;
import ch.ethz.idsc.amodeus.analysis.element.StaticHelper;
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

public enum EmptyRoboTaxiStatusDistribution implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "statusDistributionEmptyRoboTaxi";
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
        
        Tensor valuesComplet = Transpose.of(notWithCustomer);

        /** check that all the timesteps contain all the Robo Taxis */
//        Tensor testTensor = Total.of(Transpose.of(notWithCustomer));
//        testTensor.forEach(t -> GlobalAssert.that(t.Get().number().intValue() == numberVehicles));

        /** create status Labels */
        String[] statusLablesOnly = new String[] { //
                RoboTaxiStatus.DRIVETOCUSTOMER.description(), //
                RoboTaxiStatus.REBALANCEDRIVE.description(), //
                RoboTaxiStatus.PARKING.description(), //
        };
        
        /** create Colors */
        NumberPassengerColorScheme nPCS = new NumberPassengerColorScheme(COLOR_DATA_GRADIENT_DEFAULT, colorDataIndexed);
        CustomColorDataCreator colorDataCreator = new CustomColorDataCreator();
        colorDataCreator.append(nPCS.of(RoboTaxiStatus.DRIVETOCUSTOMER));
        colorDataCreator.append(nPCS.of(RoboTaxiStatus.REBALANCEDRIVE));
        colorDataCreator.append(nPCS.of(RoboTaxiStatus.PARKING));
        ColorDataIndexed colorScheme = colorDataCreator.getColorDataIndexed();

        /** create scaling factor */
        Double[] scale = new Double[statusLablesOnly.length];
        Arrays.fill(scale, 1.0);

        /** plot image */
        try {
            StackedTimeChart.of( //
                    relativeDirectory, //
                    FILENAME, //
                    "Empty Driving Vehicles", //
                    StaticHelper.FILTER_ON, //
                    StaticHelper.FILTERSIZE, //
                    scale, //
                    statusLablesOnly, //
                    "RoboTaxis", //
                    time, //
                    valuesComplet, //
                    colorScheme);
        } catch (Exception e1) {
            System.err.println("The Modular empty vehicles Tensor was not carried out!!");
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
