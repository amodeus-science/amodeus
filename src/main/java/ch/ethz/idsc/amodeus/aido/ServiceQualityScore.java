/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import java.util.Properties;

import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/* package */ class ServiceQualityScore extends LinComScore {
    protected static Tensor setAlpha(Properties properties) {
        return Tensors.of( //
                Scalars.fromString(properties.getProperty("alpha1")), //
                Scalars.fromString(properties.getProperty("alpha2")));
    }

    /* package */ ServiceQualityScore(Properties properties) {
        super(setAlpha(properties));
    }

}
