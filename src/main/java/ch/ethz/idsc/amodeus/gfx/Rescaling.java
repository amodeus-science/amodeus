/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.gfx;

import ch.ethz.idsc.amodeus.util.math.Ranking;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.NormalizeUnlessZero;
import ch.ethz.idsc.tensor.opt.TensorUnaryOperator;
import ch.ethz.idsc.tensor.red.Min;
import ch.ethz.idsc.tensor.red.Norm;
import ch.ethz.idsc.tensor.sca.ScalarUnaryOperator;

class Cap implements TensorUnaryOperator {
    private static final TensorUnaryOperator NORMALIZE = NormalizeUnlessZero.with(Norm._1);
    // ---
    private final Scalar scalar;
    private final ScalarUnaryOperator scalarUnaryOperator;

    public Cap(Scalar scalar) {
        this.scalar = scalar;
        scalarUnaryOperator = Min.function(scalar);

    }

    @Override
    public Tensor apply(Tensor vector) {
        return NORMALIZE.apply(vector).map(scalarUnaryOperator).divide(scalar);
    }
}

enum Rank implements TensorUnaryOperator {
    INSTANCE;

    @Override
    public Tensor apply(Tensor vector) {
        return Ranking.of(vector).divide(RealScalar.of(vector.length()));
    }

}

/* package */ enum Rescaling implements TensorUnaryOperator {
    CAP_01(new Cap(RealScalar.of(.01))), //
    CAP_02(new Cap(RealScalar.of(.02))), //
    CAP_05(new Cap(RealScalar.of(.05))), //
    CAP_10(new Cap(RealScalar.of(.10))), //
    CAP_20(new Cap(RealScalar.of(.20))), //
    CAP_50(new Cap(RealScalar.of(.50))), //
    ONE(NormalizeUnlessZero.with(Norm._1)), //
    /** earlier, the 1-norm was used, but the infinity-norm is preferred for large fleets */
    MAX(NormalizeUnlessZero.with(Norm.INFINITY)), //
    RANK(Rank.INSTANCE);
    // ---
    private final TensorUnaryOperator tensorUnaryOperator;

    /** @param tensorUnaryOperator to which only non-empty vector will be passed */
    private Rescaling(TensorUnaryOperator tensorUnaryOperator) {
        this.tensorUnaryOperator = tensorUnaryOperator;
    }

    @Override
    public Tensor apply(Tensor vector) {
        if (Tensors.isEmpty(vector))
            return Tensors.empty();
        return tensorUnaryOperator.apply(vector);
    }

}
