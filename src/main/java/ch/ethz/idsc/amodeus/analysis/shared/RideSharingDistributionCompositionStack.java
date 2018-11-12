/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.shared;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.element.NumberPassengersAnalysis;
import ch.ethz.idsc.amodeus.analysis.plot.CompositionStack;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.analysis.report.TtlValIdent;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.red.Total;

public enum RideSharingDistributionCompositionStack implements AnalysisExport, TotalValueAppender {
    INSTANCE;

    public static final String FILENAME = "SharedDistributionTotal";
    private double requestSharedRate = -1;

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        NumberPassengersAnalysis nPA = analysisSummary.getNumberPassengersAnalysis();

        /** Get Values */
        Tensor sharedDistribution = nPA.getSharedOthersDistribution();
        double totalNumberPassengers = Total.of(sharedDistribution).Get().number().doubleValue();
        double[] values = sharedDistribution.stream().mapToDouble(s -> s.Get().number().doubleValue() / totalNumberPassengers).toArray();
        requestSharedRate = 1 - values[0];

        /** Create Labels */
        String[] labels = new String[values.length];
        IntStream.range(0, values.length).forEach(i -> labels[i] = i + 1 + " Passengers");

        /** create Colors */
        NumberPassengerColorScheme nPCS = new NumberPassengerColorScheme(NumberPassengerStatusDistribution.COLOR_DATA_GRADIENT_DEFAULT, colorDataIndexed);
        CustomColorDataCreator colorDataCreator = new CustomColorDataCreator();
        IntStream.range(1, values.length + 1).forEach(i -> colorDataCreator.append(nPCS.of(RealScalar.of(i))));

        try {
            CompositionStack.of( //
                    relativeDirectory, //
                    FILENAME, //
                    "Ride Sharing Distribution, fraction of Requests", //
                    values, //
                    labels, //
                    colorDataCreator.getColorDataIndexed());
        } catch (Exception e) {
            System.err.println("The Stacked Distance Plot was not successfull");
            e.printStackTrace();
        }
    }

    @Override
    public Map<TotalValueIdentifier, String> getTotalValues() {
        Map<TotalValueIdentifier, String> map = new HashMap<>();
        map.put(TtlValIdent.REQUESTSHAREDRATE, String.valueOf(requestSharedRate));
        return map;
    }
}
