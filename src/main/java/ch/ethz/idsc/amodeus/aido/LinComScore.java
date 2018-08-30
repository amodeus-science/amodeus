package ch.ethz.idsc.amodeus.aido;

import java.util.Properties;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;

public abstract class LinComScore {

    protected Tensor alpha = null;
    private Scalar score = RealScalar.ZERO;
    private Scalar scorePrev = RealScalar.ZERO;

    protected abstract void setAlpha(Properties scrprm);

    public void update(Tensor measurement) {
        scorePrev = score;
        score = ((Scalar) measurement.dot(alpha)).add(scorePrev);
    }

    public Scalar getScoreIntg() {
        return (Scalar) score.copy();
    }

    public Scalar getScoreDiff() {
        Scalar scoreDiff = score.subtract(scorePrev);
        GlobalAssert.that(Scalars.lessEquals(scoreDiff, RealScalar.ZERO));
        return scoreDiff;
    }

}
