package ch.ethz.matsim.av.config;

import org.matsim.core.config.ReflectiveConfigGroup;

public class AVScoringParameterSet extends ReflectiveConfigGroup {
	static public final String GROUP_NAME = "scoring";

	static public final String SUBPOPULATION = "subpopulation";
	static public final String STUCK_UTILITY = "stuckUtility";
	static public final String MARGINAL_UTILITY_OF_WAITING = "marginalUtilityOfWaiting";

	private String subpopulation = null;
	private double stuckUtility = -1000.0;
	private double marginalUtilityOfWaiting = -1.0;

	public AVScoringParameterSet() {
		super(GROUP_NAME);
	}

	@StringGetter(STUCK_UTILITY)
	public double getStuckUtility() {
		return stuckUtility;
	}

	@StringSetter(STUCK_UTILITY)
	public void setStuckUtility(double stuckUtility) {
		this.stuckUtility = stuckUtility;
	}

	@StringGetter(MARGINAL_UTILITY_OF_WAITING)
	public double getMarginalUtilityOfWaitingTime() {
		return marginalUtilityOfWaiting;
	}

	@StringSetter(MARGINAL_UTILITY_OF_WAITING)
	public void setMarginalUtilityOfWaitingTime(double marginalUtilityOfWaitingTime) {
		this.marginalUtilityOfWaiting = marginalUtilityOfWaitingTime;
	}

	@StringGetter(SUBPOPULATION)
	public String getSubpopulation() {
		return subpopulation;
	}

	@StringSetter(SUBPOPULATION)
	public void setSubpopulation(String subpopulation) {
		this.subpopulation = subpopulation;
	}
}
