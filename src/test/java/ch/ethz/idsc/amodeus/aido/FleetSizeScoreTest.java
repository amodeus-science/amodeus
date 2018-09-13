/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.red.Total;
import junit.framework.TestCase;

public class FleetSizeScoreTest extends TestCase {

    public void testFulfill() {
        FleetSizeScore fsc = new FleetSizeScore(ScoreParameters.GLOBAL, 10, 27);
        Tensor sumDiff = Tensors.empty();
        for (int i = 0; i < 10; ++i) {
            fsc.update(Quantity.of(100, SI.SECOND), Quantity.of(i, SI.SECOND));
            sumDiff.append(fsc.getScoreDiff());
        }
        assertEquals(-27, ((Scalar) Total.of(sumDiff)).number().intValue());
    }

    public void testFail() {
        FleetSizeScore fsc = new FleetSizeScore(ScoreParameters.GLOBAL, 10, 27);
        Tensor sumDiff = Tensors.empty();
        for (int i = 0; i < 10; ++i) {
            fsc.update(Quantity.of(500, SI.SECOND), Quantity.of(i, SI.SECOND));
            sumDiff.append(fsc.getScoreDiff());
        }
        assertEquals(Double.NEGATIVE_INFINITY, ((Scalar) Total.of(sumDiff)).number().doubleValue());
    }

    public void testFail2() {
        FleetSizeScore fsc = new FleetSizeScore(ScoreParameters.GLOBAL, 10, 27);
        try {
            fsc.update(Quantity.of(-2, SI.SECOND), Quantity.of(2, SI.SECOND));
            assertTrue(false);
        } catch (Exception exception) {
            // ---
        }
        try {
            fsc.update(Quantity.of(2, SI.SECOND), Quantity.of(-2, SI.SECOND));
            assertTrue(false);
        } catch (Exception exception) {
            // ---
        }
    }
}
