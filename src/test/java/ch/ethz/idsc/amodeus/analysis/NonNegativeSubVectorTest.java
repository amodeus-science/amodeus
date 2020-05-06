/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import ch.ethz.idsc.amodeus.util.math.NonNegativeSubVector;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import junit.framework.TestCase;

public class NonNegativeSubVectorTest extends TestCase {
    public void testSimple() {
        Tensor tensor = NonNegativeSubVector.of(Tensors.fromString("{2[m],-3[s],0[N],9[A]}"));
        assertEquals(tensor, Tensors.fromString("{2[m],0[N],9[A]}"));
    }
}
