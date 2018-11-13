/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.UnitSaveUtils;
import ch.ethz.idsc.amodeus.analysis.plot.TimeChart;
import ch.ethz.idsc.amodeus.util.io.SaveFormats;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Transpose;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.red.Max;

public enum WaitingCustomerExport implements AnalysisExport {
    INSTANCE;

    private final String identifier = "waitingCustPerTime";
    public static final String FILENAME = "numberCustomersPlot";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relDir, ColorDataIndexed colorDataIndexed) {
        TravelTimeAnalysis travelTime = analysisSummary.getTravelTimeAnalysis();

        /** save graphics */
        double maxWaiting = travelTime.waitingCustomers.flatten(-1) // integer value, double for compatibility
                .reduce(Max::of).get().Get().number().doubleValue();

        String xAxisLabel = "Time";
        String yAxisLabel = "Waiting Customers [#]";
        double[] scale = new double[] { 1.0 };

        try {
            TimeChart.of(relDir, FILENAME, "Waiting Customers per Day Time", //
                    StaticHelper.FILTER_ON, StaticHelper.FILTERSIZE, scale, new String[] { "# waiting customers" }, //
                    xAxisLabel, yAxisLabel, travelTime.time, Transpose.of(Tensors.of(travelTime.waitingCustomers)), //
                    new Double[] { 0.0, maxWaiting + 1 }, colorDataIndexed);
        } catch (Exception e) {
            System.err.println("Binned Waiting Times Plot was unsucessfull!");
            e.printStackTrace();
        }

        /** save information for processing in other tools */
        try {
            /** request information */
            Tensor table = Transpose.of(Tensors.of(travelTime.time, travelTime.waitingCustomers));
            UnitSaveUtils.saveFile(table, identifier, relDir);
            File dataFolder = new File(relDir, identifier);
            GlobalAssert.that(dataFolder.isDirectory());
            SaveFormats.CSV.save(Tensors.fromString("time, # waiting customers"), dataFolder, "description");
        } catch (Exception e) {
            System.err.println("Error saving the waiting customers per time step.");
            e.printStackTrace(System.out);
        }
    }
}
