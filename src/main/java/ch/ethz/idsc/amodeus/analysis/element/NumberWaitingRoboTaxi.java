package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;
import java.util.Arrays;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.UnitSaveUtils;
import ch.ethz.idsc.amodeus.analysis.plot.StackedTimeChart;
import ch.ethz.idsc.amodeus.analysis.plot.TimeChart;
import ch.ethz.idsc.amodeus.analysis.shared.CustomColorDataCreator;
import ch.ethz.idsc.amodeus.analysis.shared.NumberPassengerColorScheme;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.util.io.SaveFormats;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Join;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.img.ColorDataGradients;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.opt.ScalarTensorFunction;
import ch.ethz.idsc.tensor.red.Max;

public enum NumberWaitingRoboTaxi implements AnalysisExport {
    INSTANCE;
	
	private final String identifier = "waitingVehicles";

    public static final String FILENAME = "numberWaitingRoboTaxi";
    // TODO might be done dependent on the Secnario Options
    public static final ScalarTensorFunction COLOR_DATA_GRADIENT_DEFAULT = ColorDataGradients.SUNSET;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {

        NumberPassengersAnalysis nPA = analysisSummary.getNumberPassengersAnalysis();
        StatusDistributionElement sDE = analysisSummary.getStatusDistribution();

        /** create Time vector */
        Tensor time = nPA.getTime();
        GlobalAssert.that(time.equals(sDE.time));

        /** create values */
        Tensor statusesTensor = Transpose.of(sDE.statusTensor);
        Tensor waiting = statusesTensor.get(RoboTaxiStatus.WAITING.ordinal());
        
        double maxWaiting = waiting.flatten(-1) // integer value, double for compatibility
                .reduce(Max::of).get().Get().number().doubleValue();
        
        /** check that all the timesteps contain all the Robo Taxis */
//        Tensor testTensor = Total.of(Transpose.of(notWithCustomer));
//        testTensor.forEach(t -> GlobalAssert.that(t.Get().number().intValue() == numberVehicles));
        
        
        String xAxisLabel = "Time";
        String yAxisLabel = "Waiting Customers [#]";
        double[] scale = new double[] { 1.0 };


        /** plot image */
        try {
            TimeChart.of(relativeDirectory, FILENAME, "Waiting Vehicles per Day Time", //
                    StaticHelper.FILTER_ON, StaticHelper.FILTERSIZE, scale, new String[] { "# waiting vehicles" }, //
                    xAxisLabel, yAxisLabel, time, Transpose.of(Tensors.of(waiting)), //
                    new Double[] { 0.0, maxWaiting + 1 }, colorDataIndexed);
        } catch (Exception e) {
            System.err.println("Binned Waiting Times Plot was unsucessfull!");
            e.printStackTrace();
        }

        /** save information for processing in other tools */
        try {
            /** request information */
            Tensor table = Transpose.of(Tensors.of(time, waiting));
            UnitSaveUtils.saveFile(table, identifier, relativeDirectory);
            File dataFolder = new File(relativeDirectory, identifier);
            GlobalAssert.that(dataFolder.isDirectory());
            SaveFormats.CSV.save(Tensors.fromString("time, # waiting customers"), dataFolder, "description");
        } catch (Exception e) {
            System.err.println("Error saving the waiting customers per time step.");
            e.printStackTrace(System.out);
        }

    }
}
