/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.idsc.tensor.red.Quantile;

public enum AnalysisHelper {
    ;

    public static Tensor quantiles(Tensor submission, Tensor param) {
        if (Tensors.isEmpty(submission))
            return Array.zeros(param.length());
        return param.map(Quantile.of(submission));
    }

    public static Scalar means(Tensor submission) {
        return Tensors.isEmpty(submission) ? RealScalar.ZERO : (Scalar) Mean.of(submission);
    }
}
