/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;

/** used to compute scores which are a linear combination
 * of variables and weights */
/* package */ abstract class LinComScore {
    protected final Tensor alpha;
    /** the weight */
    private Scalar score = RealScalar.ZERO;
    private Scalar scorePrev = RealScalar.ZERO;

    protected LinComScore(Tensor alpha) {
        this.alpha = alpha;
    }

    public void update(Tensor measurement) {
        scorePrev = score;
        score = ((Scalar) measurement.dot(alpha)).add(scorePrev);
    }

    public Scalar getScoreIntg() {
        return score;
    }

    public Scalar getScoreDiff() {
        Scalar scoreDiff = score.subtract(scorePrev);
        // the check below is specific to the reward function used in the aido competition
        // the check below is not strictly required in general
        GlobalAssert.that(Scalars.lessEquals(scoreDiff, RealScalar.ZERO));
        return scoreDiff;
    }
}