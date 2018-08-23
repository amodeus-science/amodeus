/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.io.StringScalar;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.sca.ScalarUnaryOperator;

enum UnitStringScalar implements ScalarUnaryOperator {
    FUNCTION;
    private static final Scalar EMPTY = StringScalar.of("\"\"");

    @Override
    public Scalar apply(Scalar scalar) {
        if (scalar instanceof Quantity)
            return StringScalar.of("\"" + ((Quantity) scalar).unit() + "\"");
        return EMPTY;
    }

    public static void main(String[] args) {
        {
            Scalar apply = UnitStringScalar.FUNCTION.apply(Quantity.of(3, "m*s"));
            System.out.println(apply);
        }
        {
            Scalar apply = UnitStringScalar.FUNCTION.apply(RealScalar.of(3));
            System.out.println(apply);
        }
    }

}
