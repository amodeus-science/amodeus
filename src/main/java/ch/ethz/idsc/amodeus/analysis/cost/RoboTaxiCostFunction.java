/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.cost;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public interface RoboTaxiCostFunction extends TotalValueAppender {
    /** gives back the annual fleet costs for this Cost function.
     * It is saved in the TotalValues.properties file
     * 
     * @return */
    double annualFleetCosts();

    /** Returns the total value identifier where the annual fleet costs are stored in the total values file.
     * 
     * @return */
    TotalValueIdentifier getTotalValueIdentifier();

    /** Gives the possibility to save multiple values from the Cost function. e.g. total fleet costs, overhead cost, wages,...
     * 
     * default: map with one entry which is the identifier in the getTotalValueIdentifier() function with the value annualFleetCost()
     * 
     * @return a map with the */
    @Override
    default Map<TotalValueIdentifier, String> getTotalValues() {
        Map<TotalValueIdentifier, String> totalCostValues = new HashMap<>();
        GlobalAssert.that(getTotalValueIdentifier() != null);
        totalCostValues.put(getTotalValueIdentifier(), String.valueOf(annualFleetCosts()));
        return totalCostValues;
    }

    /** is the possibility to store the analysis summary in the Cost function. this function is called first if the cost function is added to the Amodeus Analysis
     * 
     * @param analysisSummary */
    void setAnalysisSummary(AnalysisSummary analysisSummary);
}
