/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.VectorQ;
import ch.ethz.idsc.tensor.qty.Quantity;

enum StaticHelper {
    ;
    /** distances zero */
    static final Tensor ZEROS = Tensors.of(Quantity.of(0, SI.METER), Quantity.of(0, SI.METER)).unmodifiable();

    /** check that score monotonously increasing with respect to time */
    static void requirePositiveOrZero(Tensor scoreAdd) {
        VectorQ.requireLength(scoreAdd, 3);
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.SECOND), scoreAdd.Get(0)));
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.METER), scoreAdd.Get(1)));
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.METER), scoreAdd.Get(2)));
    }
}
