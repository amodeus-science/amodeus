/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.cost;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.plot.ColorScheme;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifiersAmodeus;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class FleetCostElement implements AnalysisExport, TotalValueAppender {

    // total Values for TotalValuesFile
    private final Map<TotalValueIdentifier, String> totalValues = new HashMap<>();

    private final RoboTaxiCostFunction roboTaxiCostFunction;

    public FleetCostElement(RoboTaxiCostFunction roboTaxiCostFunction, RoboTaxiCostParameters roboTaxiCostParameters) {
        this.roboTaxiCostFunction = roboTaxiCostFunction;
        roboTaxiCostFunction.setCostParameters(roboTaxiCostParameters);
    }

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorScheme colorScheme) {
        roboTaxiCostFunction.setAnalysisSummary(analysisSummary);
    }

    @Override
    public Map<TotalValueIdentifier, String> getTotalValues() {
        for (Entry<TotalValueIdentifier, String> entry : roboTaxiCostFunction.getTotalValues().entrySet()) {
            if (entry.getKey().equals(TotalValueIdentifiersAmodeus.ANNUALFLEETCOST)) {
                totalValues.put(entry.getKey(), entry.getValue());
            } else {
                GlobalAssert.that(!TotalValueIdentifiersAmodeus.contains(entry.getKey()));
                totalValues.put(entry.getKey(), entry.getValue());
            }
        }
        if (totalValues.containsKey(TotalValueIdentifiersAmodeus.ANNUALFLEETCOST)) {
            if (Double.valueOf(totalValues.get(TotalValueIdentifiersAmodeus.ANNUALFLEETCOST)).equals(0.0)) {
                totalValues.put(TotalValueIdentifiersAmodeus.ANNUALFLEETCOST, "Change the Cost Function to get a Value");
            }
        }
        return totalValues;
    }
}
