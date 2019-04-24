/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import java.util.NavigableSet;
import java.util.TreeSet;

import ch.ethz.idsc.amodeus.dispatcher.core.RStatusHelper;
import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.NormalizeUnlessZero;
import ch.ethz.idsc.tensor.opt.TensorUnaryOperator;
import ch.ethz.idsc.tensor.red.Max;
import ch.ethz.idsc.tensor.red.Mean;
import ch.ethz.idsc.tensor.red.Median;
import ch.ethz.idsc.tensor.red.Norm;

/* package */ enum StaticHelper {
    ;
    private static final TensorUnaryOperator NORMALIZE = NormalizeUnlessZero.with(Norm._1);

    public static Scalar meanOrZero(Tensor vector) {
        if (Tensors.isEmpty(vector))
            return RealScalar.ZERO;
        return Mean.of(vector).Get();
    }

    public static Scalar medianOrZero(Tensor vector) {
        if (vector.length() == 0)
            return RealScalar.ZERO;
        return Median.of(vector).Get();
    }

    public static Scalar maxOrZero(Tensor vector) {
        return vector.flatten(0) //
                .map(Scalar.class::cast) //
                .reduce(Max::of).orElse(RealScalar.ZERO);
    }

    /** @param vector with non-negative entries
     * @return */
    @Deprecated // do not use this anymore as it provides very bad results
    // for large numbers of virtual nodes and large scale visualizations.
    public static Tensor normalize1Norm224(Tensor vector) {
        if (Tensors.isEmpty(vector))
            return Tensors.empty();
        return NORMALIZE.apply(vector).multiply(RealScalar.of(224));
    }

    /** @param vector with non-negative entries with higher contrast. The color value of
     *            224 is always placed at the maximum value of the current view.
     * @return */
    public static Tensor normalize1Norm224Contrast(Tensor vector) {
        NavigableSet<Scalar> set = new TreeSet<>();
        vector.flatten(-1).forEach(s -> set.add((Scalar) s));
        Scalar maxVal = set.last();
        Tensor returnT = vector.copy();
        for (int i = 0; i < returnT.length(); ++i) {
            returnT.set(vector.Get(i).divide(maxVal).multiply(RationalScalar.of(224, 1)), i);
        }
        return returnT;
    }

    public static boolean isWaiting(RequestContainer requestContainer) {
        return RStatusHelper.unserviced(requestContainer.requestStatus);
    }

}
