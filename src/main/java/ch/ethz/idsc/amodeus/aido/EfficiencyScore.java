/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.aido;

/* package */ class EfficiencyScore extends LinComScore {
    /* package */ EfficiencyScore(ScoreParameters scoreParameters) {
        super(scoreParameters.alpha34);
    }
}
