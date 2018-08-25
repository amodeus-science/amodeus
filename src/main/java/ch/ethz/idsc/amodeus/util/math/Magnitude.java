/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.math;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.qty.QuantityMagnitude;
import ch.ethz.idsc.tensor.qty.Unit;
import ch.ethz.idsc.tensor.sca.ScalarUnaryOperator;

/** converts a {@link Quantity} to a unit less {@link Scalar}
 * or throws an exception if the conversion is not possible */
public enum Magnitude implements ScalarUnaryOperator {
    ONE(SI.ONE), //
    // ---
    METER(SI.METER), //
    SECOND(SI.SECOND), //
    // ---
    VELOCITY(SI.VELOCITY), //
    ;
    // ---
    private final ScalarUnaryOperator scalarUnaryOperator;

    private Magnitude(Unit unit) {
        scalarUnaryOperator = QuantityMagnitude.SI().in(unit);
    }

    @Override // from ScalarUnaryOperator
    public Scalar apply(Scalar scalar) {
        return scalarUnaryOperator.apply(scalar);
    }

    /** @param scalar
     * @return double value of given scalar quantity after conversion to given unit */
    public double toDouble(Scalar scalar) {
        return apply(scalar).number().doubleValue();
    }

    /** @param scalar
     * @return short value of given scalar quantity after conversion to given unit */
    public short toShort(Scalar scalar) {
        return apply(scalar).number().shortValue();
    }

    /** @param scalar
     * @return int value of given scalar quantity after conversion to given unit */
    public int toInt(Scalar scalar) {
        return apply(scalar).number().intValue();
    }

    /** @param scalar
     * @return long value of given scalar quantity after conversion to given unit */
    public long toLong(Scalar scalar) {
        return apply(scalar).number().longValue();
    }
}
