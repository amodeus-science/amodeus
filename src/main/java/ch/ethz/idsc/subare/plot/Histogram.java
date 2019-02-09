/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.subare.plot;

import java.util.function.Function;

import org.jfree.chart.JFreeChart;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

/** inspired by
 * <a href="https://reference.wolfram.com/language/ref/Histogram.html">Histogram</a> */
public enum Histogram {
    ;

    public static JFreeChart of(VisualSet visualSet) {
        return of(visualSet, false);
    }

    /* package */ static JFreeChart of(VisualSet visualSet, boolean stacked) {
        Function<Scalar, String> naming = visualSet.visualRows().stream() //
                .allMatch(visualRow -> visualRow.points().length() == 1 // <- TODO JOEL this usually evaluates to false
                        && visualSet.getVisualRow(0).points().get(Tensor.ALL, 0).equals(visualRow.points().get(Tensor.ALL, 0))) //
                                ? s -> ""
                                : Scalar::toString;
        naming = Scalar::toString;
        return JFreeCharts.barChart(visualSet, stacked, naming);
    }

    public static JFreeChart of(VisualSet visualSet, Function<Scalar, String> naming) {
        return JFreeCharts.barChart(visualSet, false, naming);
    }
}
