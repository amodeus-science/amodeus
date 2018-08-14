/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.cost;

import java.util.Map;

public interface RoboTaxiCostParameters {
    // TODO comment
    Map<CostParameterIdentifier, Double> getCostParameters();

    Double getCostParameter(CostParameterIdentifier costParameterIdentifier);
}
