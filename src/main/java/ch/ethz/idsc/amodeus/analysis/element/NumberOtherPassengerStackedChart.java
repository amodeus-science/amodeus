/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;
import java.util.stream.IntStream;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.plot.ColorScheme;
import ch.ethz.idsc.amodeus.analysis.plot.CompositionStack;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.red.Max;

public enum NumberOtherPassengerStackedChart implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "numberOtherPassengerStacked";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorScheme colorScheme) {
        NumberPassengersAnalysis nPA = analysisSummary.getNumberPassengersAnalysis();

        int maxNumberOtherPassenger = nPA.getSharedOthersPerRequest().flatten(-1).reduce(Max::of).get().Get().number().intValue();
        String[] labels = new String[maxNumberOtherPassenger + 1];
        IntStream.range(0, maxNumberOtherPassenger + 1).forEach(i -> labels[i] = i + " other Passenger");

        double[] values = new double[maxNumberOtherPassenger + 1];
        
        Tensor disTensor = nPA.getSharedOthersDistribution().divide(RealScalar.of(nPA.getSharedOthersPerRequest().length()));
        IntStream.range(0, maxNumberOtherPassenger + 1).forEach(i -> values[i] = disTensor.Get(i).number().intValue());

        try {
            CompositionStack.of( //
                    relativeDirectory, //
                    FILENAME, //
                    "Number of Other Passengers in the RoboTaxi", //
                    values, //
                    labels, //
                    ColorScheme.LONG);
        } catch (Exception e) {
            System.err.println("The Stacked Distance Plot was not successfull");
            e.printStackTrace();
        }
    }
}
