/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.img.MeanFilter;

public enum AnalysisMeanFilter {
    ;

    /** for small scenarios, a filter is necessary to obtain smooth waiting times plots */
    private static final boolean FILTER_ON = true;

    private static final int FILTER_SIZE = 50;

    public static Tensor of(Tensor values) {
        return FILTER_ON //
                ? MeanFilter.of(values, FILTER_SIZE) : values;
    }

}
