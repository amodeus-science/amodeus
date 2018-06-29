package ch.ethz.idsc.amodeus.analysis.cost;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.amodeus.analysis.plot.ColorScheme;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifiersAmodeus;

public class FleetCostElement implements AnalysisExport, TotalValueAppender {

    // total Values for TotalValuesFile
    private final Map<TotalValueIdentifier, String> totalValues = new HashMap<>();

    private final RoboTaxiCostFunction roboTaxiCostFunction;
    private final RoboTaxiCostParameters roboTaxiCostParameters;
    private double annualFleetCost;

    public FleetCostElement(RoboTaxiCostFunction roboTaxiCostFunction, RoboTaxiCostParameters roboTaxiCostParameters) {
        this.roboTaxiCostFunction = roboTaxiCostFunction;
        this.roboTaxiCostParameters = roboTaxiCostParameters;
    }

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorScheme colorScheme) {
        annualFleetCost = roboTaxiCostFunction.annualFleetCosts(analysisSummary, roboTaxiCostParameters);
    }

    @Override
    public Map<TotalValueIdentifier, String> getTotalValues() {
        totalValues.put(TotalValueIdentifiersAmodeus.ANNUALFLEETCOST, String.valueOf(annualFleetCost));

        if (annualFleetCost == 0.0) {
            totalValues.put(TotalValueIdentifiersAmodeus.ANNUALFLEETCOST, "Change the Cost Function to get a Value");
        }
        return totalValues;
    }
}
