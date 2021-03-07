package amodeus.amodeus.util.math;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Unprotect;

public enum Scalar2Number {
    ;
    public static Number of(Scalar scalar) {
        return Unprotect.withoutUnit(scalar).number();
    }

}
