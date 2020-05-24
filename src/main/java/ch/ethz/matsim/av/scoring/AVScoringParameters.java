package ch.ethz.matsim.av.scoring;

import ch.ethz.matsim.av.config.modal.AmodeusScoringConfig;

public final class AVScoringParameters {
    public final double marginalUtilityOfWaiting_s;
    public final double stuckUtility;

    public AVScoringParameters(double marginalUtilityOfWaiting_s, double stuckUtility) {
        this.marginalUtilityOfWaiting_s = marginalUtilityOfWaiting_s;
        this.stuckUtility = stuckUtility;
    }

    static AVScoringParameters fromParameterSet(AmodeusScoringConfig set) {
        return new AVScoringParameters(set.getMarginalUtilityOfWaitingTime() / 3600.0, set.getStuckUtility());
    }
}
