/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.cost;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;

public interface RoboTaxiCostFunction {
    double annualFleetCosts(AnalysisSummary analysisSummary, RoboTaxiCostParameters roboTaxiCostParameters);
}
