/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.math;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.api.ScalarUnaryOperator;
import ch.ethz.idsc.tensor.io.StringScalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public enum UnitStringScalar implements ScalarUnaryOperator {
    FUNCTION;

    // ---
    private static final Scalar EMPTY = StringScalar.of("\"\"");

    @Override
    public Scalar apply(Scalar scalar) {
        if (scalar instanceof Quantity)
            return StringScalar.of("\"" + ((Quantity) scalar).unit() + "\"");
        return EMPTY;
    }

}
