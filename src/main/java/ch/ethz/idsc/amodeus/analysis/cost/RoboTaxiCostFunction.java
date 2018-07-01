/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.cost;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public interface RoboTaxiCostFunction extends TotalValueAppender {
    double annualFleetCosts();

    TotalValueIdentifier getTotalValueIdentifier();

    @Override
    default Map<TotalValueIdentifier, String> getTotalValues() {
        Map<TotalValueIdentifier, String> totalCostValues = new HashMap<>();
        GlobalAssert.that(getTotalValueIdentifier() != null);
        totalCostValues.put(getTotalValueIdentifier(), String.valueOf(annualFleetCosts()));
        return totalCostValues;
    }

    void setAnalysisSummary(AnalysisSummary analysisSummary);

    void setCostParameters(RoboTaxiCostParameters cp);
}
