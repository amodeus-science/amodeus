package ch.ethz.idsc.amodeus.analysis;

import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.qty.Unit;

/* package */ enum PositiveSubVector {
    ;

    public static Tensor of(Tensor vector) {
        Unit unit = getUnitof(vector.Get(0));
        return Tensors.vector(//
                vector.flatten(-1).//
                        filter(s -> Scalars.lessEquals(Quantity.of(0, unit), (Scalar) s)).//
                        map(s -> ((Scalar) s).number()).//
                        collect(Collectors.toList())).multiply(Quantity.of(1, unit));//
    }

    private static Unit getUnitof(Scalar scalar) {
        if (!(scalar instanceof Quantity))
            return SI.ONE;
        else {
            Quantity q = (Quantity) scalar;
            return q.unit();
        }
    }
}
