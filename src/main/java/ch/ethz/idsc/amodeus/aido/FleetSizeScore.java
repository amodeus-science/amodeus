package ch.ethz.idsc.amodeus.aido;

import java.util.Properties;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class FleetSizeScore {

    private final Scalar wMax;
    private final Scalar fleetSize;
    private Scalar score;

    public FleetSizeScore(Properties scrprm, int totReq, int fleetSize) {
        Scalar wmean = Quantity.of(//
                Double.parseDouble(scrprm.getProperty("wmean")), SI.SECOND.negate());
        wMax = wmean.multiply(RealScalar.of(totReq));
        this.fleetSize = RationalScalar.of(fleetSize, 1);
        this.score = this.fleetSize.negate();
    }

    public void update(Scalar totalWait) {
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.SECOND), totalWait));
        if (Scalars.lessThan(wMax, totalWait)) {
            this.score = Quantity.of(Double.NEGATIVE_INFINITY, "");
        }
    }

    public Scalar getScore() {
        return (Scalar) score.copy();
    }

}
