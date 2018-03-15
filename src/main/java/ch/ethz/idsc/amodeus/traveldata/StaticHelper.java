/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.traveldata;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.TensorRuntimeException;
import ch.ethz.idsc.tensor.alg.Normalize;
import ch.ethz.idsc.tensor.alg.TensorMap;
import ch.ethz.idsc.tensor.red.Norm;
import ch.ethz.idsc.tensor.sca.Sign;

/* package */ enum StaticHelper {
    ;

    /** @param matrix with non-negative entries
     * @return row-stochastic matrix, i.e. each row sums up to 1 */
    public static Tensor normToRowStochastic(Tensor matrix) {
        if (matrix.flatten(-1).map(Scalar.class::cast).anyMatch(Sign::isNegative))
            throw TensorRuntimeException.of(matrix);
        return TensorMap.of(row -> Normalize.unlessZero(row, Norm._1), matrix, 1);
    }

    /** @param dt
     * @param length
     * @return greatest common divisor for integers a and b */
    public static int greatestNonRestDt(int dt, int length) {
        if (length % dt == 0)
            return dt;
        return greatestNonRestDt(dt - 1, length);
    }
}
