/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.cost;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RoboTaxiCostParametersImplAmodeus implements RoboTaxiCostParameters {

    private final Map<CostParameterIdentifier, Double> costParameters = new HashMap<>();

    public RoboTaxiCostParametersImplAmodeus(Double pricePerKm, Double pricePerTrip, Double pricePerVehicleYear, Double fixedCostYear) {
        costParameters.put(CostParameterIdentifiersAmodeus.COST_KM, pricePerKm);
        costParameters.put(CostParameterIdentifiersAmodeus.COST_FIXED_ANNUAL, fixedCostYear);
        costParameters.put(CostParameterIdentifiersAmodeus.COST_TRIP, pricePerTrip);
        costParameters.put(CostParameterIdentifiersAmodeus.COST_VEHICLE_ANNUAL, pricePerVehicleYear);
    }

    public RoboTaxiCostParametersImplAmodeus(Double pricePerKm) {
        this(pricePerKm, null, null, null);
    }

    @Override
    public Map<CostParameterIdentifier, Double> getCostParameters() {
        return Collections.unmodifiableMap(costParameters);
    }

    @Override
    public Double getCostParameter(CostParameterIdentifier costParameterIdentifier) {
        return getCostParameters().get(costParameterIdentifier);
    }

}
