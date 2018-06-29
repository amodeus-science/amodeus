package ch.ethz.idsc.amodeus.analysis.cost;

import java.util.Map;

public interface RoboTaxiCostParameters {

	public Map<CostParameterIdentifier, Double> getCostParameters();

	public Double getCostParameter(CostParameterIdentifier costParameterIdentifier);
}
