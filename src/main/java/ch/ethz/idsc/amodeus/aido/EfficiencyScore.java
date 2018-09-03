/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.Properties;

import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/* package */ class EfficiencyScore extends LinComScore {
    protected static Tensor setAlpha(Properties properties) {
        return Tensors.of( //
                Scalars.fromString(properties.getProperty("alpha3")), //
                Scalars.fromString(properties.getProperty("alpha4")));
    }

    /* package */ EfficiencyScore(Properties properties) {
        super(setAlpha(properties));
    }
}
