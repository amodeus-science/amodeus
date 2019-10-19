/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.math;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Subdivide;
import junit.framework.TestCase;

public class QuantileOrZeroTest extends TestCase {
    public void testEmpty() {
        Tensor domain = Subdivide.of(0, 1, 10);
        assertEquals(domain.map(QuantileOrZero.of(Tensors.empty())), domain.map(Scalar::zero));
    }

    public void testVector() {
        Tensor domain = Subdivide.of(0, 1, 10);
        assertEquals(domain.map(QuantileOrZero.of(Tensors.vector(1, 1, 1))), domain.map(s -> RealScalar.ONE));
    }
}
