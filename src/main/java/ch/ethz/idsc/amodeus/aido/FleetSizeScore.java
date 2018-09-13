/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.qty.Quantity;

/** the fleetsize score is defined as following, in the case that the maximum waiting time
 * wMax is not violated, its toal is -fleetSize, i.e., the differences are
 * {0,-fleetSize,0,...,0}
 * 
 * if the summmed waiting time exceeds wMax, then the final score is -Infinity, the
 * differences then are
 * {0,-fleetSize,0,...,0,-Infinity,0,...,0} */
/* package */ class FleetSizeScore {
    private final Scalar wMax;
    private final Scalar fleetSize;
    private Scalar scoreFinal = Quantity.of(0, SI.ONE);
    private Scalar scoreFinalPrev = Quantity.of(0, SI.ONE);
    private Scalar timeViolate = Quantity.of(-1, SI.SECOND);
    private Scalar totalWait = Quantity.of(0, SI.SECOND);
    private boolean firstTime = true;

    public FleetSizeScore(ScoreParameters scoreParameters, int totReq, int fleetSize) {
        Scalar wmean = scoreParameters.wmean;
        wMax = wmean.multiply(RealScalar.of(totReq));
        this.fleetSize = RationalScalar.of(fleetSize, 1);
        this.scoreFinal = Quantity.of(0, SI.ONE);
    }

    public void update(Scalar incrWait, Scalar time) {
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.SECOND), incrWait));
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.SECOND), time));
        totalWait = totalWait.add(incrWait);
        if (firstTime) { /** score starts at 0, then goes to -N */
            scoreFinal = fleetSize.negate();
            firstTime = false;
        } else {
            /** update previous score */
            scoreFinalPrev = scoreFinal;
            /** first time violation takes place, augment to -Infty */
            if (Scalars.lessThan(wMax, totalWait) //
                    && !scoreFinal.equals(Quantity.of(Double.NEGATIVE_INFINITY, SI.ONE))) {
                scoreFinal = Quantity.of(Double.NEGATIVE_INFINITY, SI.ONE);
                timeViolate = time;
            }
        }
    }

    public Scalar getTimeToViolate() {
        return timeViolate;
    }

    public Scalar getScoreDiff() {
        Scalar scoreDiff = scoreFinal.subtract(scoreFinalPrev);
        if (Double.isNaN(scoreDiff.number().doubleValue()))
            return Quantity.of(0, SI.ONE);
        return scoreDiff;
    }

    public Scalar getScoreIntg() {
        return scoreFinal;
    }
}
