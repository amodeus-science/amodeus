/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.test;

import static org.junit.Assert.assertArrayEquals;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class ScalarAssert {

    private final Tensor should = Tensors.empty();
    private final Tensor actual = Tensors.empty();

    public void add(Scalar shouldEl, Scalar actualEl) {
        should.append(shouldEl);
        actual.append(actualEl);
    }

    public void consolidate() {
        GlobalAssert.that(should.length() == actual.length());

        /** print if not equal */
        for (int i = 0; i < should.length(); ++i) {
            if (!should.Get(i).equals(actual.Get(i))) {
                System.err.println("i:              " + i);
                System.err.println("required value: " + should.Get(i));
                System.err.println("actual value:   " + actual.Get(i));
            }
        }

        /** test */
        for (int i = 0; i < should.length(); ++i)
            assertArrayEquals(new Scalar[] { should.Get(i) }, new Scalar[] { actual.Get(i) });
    }
}
