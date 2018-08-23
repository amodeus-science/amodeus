/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.video;

import java.util.Arrays;

import ch.ethz.idsc.tensor.Tensors;
import junit.framework.TestCase;

public class JavaVersion8Test extends TestCase {
    public void testSimple() {
        int sum = Arrays.asList(1, 2, 3).stream().mapToInt(i -> i).sum();
        assertEquals(sum, 6);
    }

    public void testTensor() {
        assertEquals(Tensors.vector(1, 2, 3).toString(), "{1, 2, 3}");
    }
}
