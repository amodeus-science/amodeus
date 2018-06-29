/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.cost;

public enum CostParameterIdentifiersAmodeus implements CostParameterIdentifier {
    COST_KM, //
    COST_VEHICLE_ANNUAL, //
    COST_FIXED_ANNUAL, //
    COST_TRIP, //
    ;

    @Override
    public String getIdentifier() {
        return name();
    }

}
