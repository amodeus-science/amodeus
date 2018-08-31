/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.Properties;

import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class EfficiencyScore extends LinComScore {
    protected static Tensor setAlpha(Properties scrprm) {
        Scalar alpha3 = Quantity.of(Double.parseDouble(scrprm.getProperty("alpha3")), SI.SECOND.negate());
        Scalar alpha4 = Quantity.of(Double.parseDouble(scrprm.getProperty("alpha4")), SI.METER.negate());
        return Tensors.of(alpha3, alpha4);
    }

    /* package */ EfficiencyScore(Properties properties) {
        super(setAlpha(properties));
    }
}
