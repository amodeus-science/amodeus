package ch.ethz.matsim.av.scoring;

public final class AVScoringParameters {
    public final double marginalUtilityOfWaiting_s;
    public final double stuckUtility;

    public AVScoringParameters(double marginalUtilityOfWaiting_s, double stuckUtility) {
        this.marginalUtilityOfWaiting_s = marginalUtilityOfWaiting_s;
        this.stuckUtility = stuckUtility;
    }
}
