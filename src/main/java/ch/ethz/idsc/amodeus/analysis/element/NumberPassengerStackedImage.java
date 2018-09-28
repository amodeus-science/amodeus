/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.io.File;
import java.util.Arrays;
import java.util.stream.IntStream;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.plot.ColorScheme;
import ch.ethz.idsc.amodeus.analysis.plot.StackedTimeChart;
import ch.ethz.idsc.tensor.alg.Reverse;
import ch.ethz.idsc.tensor.alg.Transpose;

public enum NumberPassengerStackedImage implements AnalysisExport {
    INSTANCE;

    public static final String FILENAME = "numberPassengersOverTime";

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorScheme colorScheme) {

        NumberPassengersAnalysis nPA = analysisSummary.getNumberPassengersAnalysis();

        String[] statusLabels = new String[nPA.getMaxNumPassengers().number().intValue() + 1];
        int numBins = nPA.getMaxNumPassengers().number().intValue() + 1;
        IntStream.range(0, numBins).forEach(i -> statusLabels[i] = numBins - i - 1 + " Passenger");

        Double[] scale = new Double[statusLabels.length];
        Arrays.fill(scale, 1.0);
        ColorScheme colorSchemeLongRev = ColorScheme.LONG;
        colorSchemeLongRev.reverseFirstN(numBins - 1);
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
                    nPA.getTime(), //
                    Transpose.of(Reverse.of(Transpose.of(nPA.getPassengerDistribution()))), //
                    colorSchemeLongRev);
        } catch (Exception e1) {
            System.err.println("The Modular number Passenger Tensor was not carried out!!");
            e1.printStackTrace();
        }
    }
}
