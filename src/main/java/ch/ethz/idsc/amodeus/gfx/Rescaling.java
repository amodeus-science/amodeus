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
import ch.ethz.idsc.tensor.sca.Sign;

class CapAbs implements TensorUnaryOperator {
    private final Scalar scalar;
    private final ScalarUnaryOperator scalarUnaryOperator;

    public CapAbs(Scalar scalar) {
        this.scalar = Sign.requirePositive(scalar);
        scalarUnaryOperator = Min.function(scalar);
    }

    @Override
    public Tensor apply(Tensor vector) {
        return vector.map(scalarUnaryOperator).divide(scalar);
    }
}

class CapRel implements TensorUnaryOperator {
    private static final TensorUnaryOperator NORMALIZE = NormalizeUnlessZero.with(Norm._1);
    // ---
    private final Scalar scalar;
    private final ScalarUnaryOperator scalarUnaryOperator;

    public CapRel(Scalar scalar) {
        this.scalar = Sign.requirePositive(scalar);
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
    /** absolute caps */
    ABS_E1(new CapAbs(RealScalar.of(1E1))), //
    ABS_E2(new CapAbs(RealScalar.of(1E2))), //
    ABS_E3(new CapAbs(RealScalar.of(1E3))), //
    ABS_E4(new CapAbs(RealScalar.of(1E4))), //
    /** relative caps */
    REL_01(new CapRel(RealScalar.of(.01))), //
    REL_02(new CapRel(RealScalar.of(.02))), //
    REL_05(new CapRel(RealScalar.of(.05))), //
    REL_10(new CapRel(RealScalar.of(.10))), //
    REL_20(new CapRel(RealScalar.of(.20))), //
    REL_50(new CapRel(RealScalar.of(.50))), //
    /** 1-norm */
    ONE(NormalizeUnlessZero.with(Norm._1)), //
    /** earlier, the 1-norm was used, but the infinity-norm is preferred for large fleets */
    MAX(NormalizeUnlessZero.with(Norm.INFINITY)), //
    /** ranking */
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
