package ch.ethz.idsc.amodeus.aido;

import java.util.Properties;

import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class EfficiencyScore extends LinComScore {

    /* package */ EfficiencyScore(Properties scrprm) {
        setAlpha(scrprm);
    }

    @Override
    protected void setAlpha(Properties scrprm) {
        Scalar alpha3 = Quantity.of(Double.parseDouble(scrprm.getProperty("alpha3")), SI.SECOND.negate());
        Scalar alpha4 = Quantity.of(Double.parseDouble(scrprm.getProperty("alpha4")), SI.METER.negate());
        alpha = Tensors.of(alpha3, alpha4);
    }
}
