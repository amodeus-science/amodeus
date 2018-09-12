package ch.ethz.idsc.amodeus.analysis.element;

import java.util.stream.Collectors;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/* package */ enum PositiveSubVector {
    ;

    public static Tensor of(Tensor vector) {
        return Tensors.vector(//
                vector.flatten(-1).//
                        filter(s -> Scalars.lessEquals(RealScalar.ZERO, (Scalar) s)).//
                        map(s -> ((Scalar) s).number()).//
                        collect(Collectors.toList()));//
    }
}
