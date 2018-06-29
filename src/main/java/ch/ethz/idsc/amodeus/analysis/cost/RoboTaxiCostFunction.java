package ch.ethz.idsc.amodeus.analysis.cost;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;

public interface RoboTaxiCostFunction {
    public double annualFleetCosts(AnalysisSummary analysisSummary, RoboTaxiCostParameters roboTaxiCostParameters);

}
