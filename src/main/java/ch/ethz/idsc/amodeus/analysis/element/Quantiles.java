/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.stream.Stream;

import org.matsim.core.utils.misc.Time;

import ch.ethz.idsc.amodeus.analysis.report.HtmlGenerator;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Flatten;

/* package */ enum Quantiles {
    ;
    private static final double LOW = 0.1;
    private static final double MID = 0.5;
    private static final double UPR = 0.95;
    public static final Tensor SET = Tensors.vector(LOW, MID, UPR).unmodifiable();
    public static final String[] LBL = new String[] { //
            (LOW * 100) + "% quantile", //
            (MID * 100) + "% quantile", //
            (UPR * 100) + "% quantile", //
            "Mean" };

    // ---
    static String lblString(String textInBold) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(HtmlGenerator.bold(textInBold) + "\n\t");
        Stream.of(LBL).forEach(string -> stringBuilder.append(string + "\n\t"));
        stringBuilder.append("Maximum:");
        return stringBuilder.toString();

    }

    static String formatAggregates(Tensor aggregates) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" \n");
        Tensor vector = Flatten.of(aggregates);
        vector.stream().map(Scalar.class::cast).map(Scalar::number) //
                .mapToDouble(Number::doubleValue) //
                .forEach(scalar -> stringBuilder.append(Time.writeTime(scalar) + "\n"));
        return stringBuilder.toString();
    }
}
