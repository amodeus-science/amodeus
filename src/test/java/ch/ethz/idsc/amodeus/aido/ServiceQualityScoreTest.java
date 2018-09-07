/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.NumberQ;
import ch.ethz.idsc.tensor.qty.Quantity;
import junit.framework.TestCase;

public class ServiceQualityScoreTest extends TestCase {
    public void testSimple() {
        ServiceQualityScore efficiencyScore = new ServiceQualityScore(ScoreParameters.GLOBAL);
        NumberQ.require(efficiencyScore.alpha.Get(0).multiply(Quantity.of(3, SI.SECOND)));
        NumberQ.require(efficiencyScore.alpha.Get(1).multiply(Quantity.of(3, SI.METER)));
        try {
            NumberQ.require(efficiencyScore.alpha.Get(1).multiply(Quantity.of(3, SI.SECOND)));
            assertTrue(false);
        } catch (Exception exception) {
            // ---
        }
    }
}
