package ch.ethz.idsc.amodeus.analysis.cost;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;

public enum CostFunctionLinearCombination {
	;

	public static double annualFleetCosts(AnalysisSummary analysisSummary, RoboTaxiCostParameters cp) {
		double annualDistance = analysisSummary.getDistanceElement().totalDistance * 365;
		int numberVehicles = analysisSummary.getSimulationInformationElement().vehicleSize();
		int numberRequests = analysisSummary.getSimulationInformationElement().reqsize();
		double Ckm = cp.getCostParameter(CostParameterIdentifiersAmodeus.COST_KM);
		double CTrip = cp.getCostParameter(CostParameterIdentifiersAmodeus.COST_TRIP);
		double CVehicle = cp.getCostParameter(CostParameterIdentifiersAmodeus.COST_VEHICLE_ANNUAL);
		double CFix = cp.getCostParameter(CostParameterIdentifiersAmodeus.COST_FIXED_ANNUAL);
		return CFix + Ckm * annualDistance + CTrip * numberRequests + CVehicle * numberVehicles;
	}

}
