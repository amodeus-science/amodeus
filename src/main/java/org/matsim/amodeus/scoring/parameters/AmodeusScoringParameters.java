package org.matsim.amodeus.scoring.parameters;

public class AmodeusScoringParameters {
    public final double marginalUtilityOfTravelTime;
    public final double marginalUtilityOfWaitingTime;
    public final double stuckUtility;
    public final double marginalUtilityOfMoney;
    public final boolean hasMonetaryDistanceRate;

    AmodeusScoringParameters(double marginalUtilityOfTravelTime, double marginalUtilityOfWaitingTime, double stuckUtility, double marginalUtilityOfMoney,
            boolean hasMonetaryDistanceRate) {
        this.marginalUtilityOfTravelTime = marginalUtilityOfTravelTime;
        this.marginalUtilityOfWaitingTime = marginalUtilityOfWaitingTime;
        this.stuckUtility = stuckUtility;
        this.marginalUtilityOfMoney = marginalUtilityOfMoney;
        this.hasMonetaryDistanceRate = hasMonetaryDistanceRate;
    }
}
