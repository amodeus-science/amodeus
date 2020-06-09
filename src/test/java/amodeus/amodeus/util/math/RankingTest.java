/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class RankingTest {

    @Test
    public void test() {
        Tensor vector = Tensors.vector(3, 4, 0, 7, 4, 0, 8, 7);
        Tensor rankin = Ranking.of(vector);
        assertEquals(rankin, Tensors.vector(2, 3, 0, 5, 3, 0, 7, 5));
    }

}
