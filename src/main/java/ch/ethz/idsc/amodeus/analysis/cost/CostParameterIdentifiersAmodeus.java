package ch.ethz.idsc.amodeus.analysis.cost;

public enum CostParameterIdentifiersAmodeus implements CostParameterIdentifier {
	COST_KM, //
	COST_VEHICLE_ANNUAL, //
	COST_FIXED_ANNUAL, //
	COST_TRIP, //
	;

	String identifier;

	private CostParameterIdentifiersAmodeus() {
		identifier = name();
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

}
