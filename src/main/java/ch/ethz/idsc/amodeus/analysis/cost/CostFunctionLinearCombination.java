/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.cost;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class CostFunctionLinearCombination implements RoboTaxiCostFunction {
    private final TotalValueIdentifier totalValueIdentifier;

    private AnalysisSummary analysisSummary = null;
    private RoboTaxiCostParameters cp = null;

    public CostFunctionLinearCombination(TotalValueIdentifier totalValueIdentifier, RoboTaxiCostParameters cp) {
        this.totalValueIdentifier = totalValueIdentifier;
        this.cp = cp;
    }

    @Override
    public double annualFleetCosts() {
        GlobalAssert.that(analysisSummary != null);
        GlobalAssert.that(cp != null);
        double annualDistance = analysisSummary.getDistanceElement().totalDistance * 365;
        int numberVehicles = analysisSummary.getSimulationInformationElement().vehicleSize();
        int numberRequests = analysisSummary.getSimulationInformationElement().reqsize();
        double Ckm = cp.getCostParameter(CostParameterIdentifiersAmodeus.COST_KM);
        double CTrip = cp.getCostParameter(CostParameterIdentifiersAmodeus.COST_TRIP);
        double CVehicle = cp.getCostParameter(CostParameterIdentifiersAmodeus.COST_VEHICLE_ANNUAL);
        double CFix = cp.getCostParameter(CostParameterIdentifiersAmodeus.COST_FIXED_ANNUAL);
        return CFix + Ckm * annualDistance + CTrip * numberRequests + CVehicle * numberVehicles;
    }

    @Override
    public TotalValueIdentifier getTotalValueIdentifier() {
        return totalValueIdentifier;
    }

    @Override
    public void setAnalysisSummary(AnalysisSummary analysisSummary) {
        this.analysisSummary = analysisSummary;
    }

}
