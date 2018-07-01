///* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
//package ch.ethz.idsc.amodeus.analysis.cost;
//
//import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
//import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
//import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifiersAmodeus;
//
//public enum RoboTaxiCostFunctionsAmodeus implements RoboTaxiCostFunction {
//    GENERAL_LINEAR_COMBINATION() {
//        @Override
//        public double annualFleetCosts(AnalysisSummary analysisSummary, RoboTaxiCostParameters cp) {
//            double annualDistance = analysisSummary.getDistanceElement().totalDistance * 365;
//            int numberVehicles = analysisSummary.getSimulationInformationElement().vehicleSize();
//            int numberRequests = analysisSummary.getSimulationInformationElement().reqsize() * 365;
//            double Ckm = cp.getCostParameter(CostParameterIdentifiersAmodeus.COST_KM);
//            double CTrip = cp.getCostParameter(CostParameterIdentifiersAmodeus.COST_TRIP);
//            double CVehicle = cp.getCostParameter(CostParameterIdentifiersAmodeus.COST_VEHICLE_ANNUAL);
//            double CFix = cp.getCostParameter(CostParameterIdentifiersAmodeus.COST_FIXED_ANNUAL);
//            return CFix + Ckm * annualDistance + CTrip * numberRequests + CVehicle * numberVehicles;
//        }
//        
//    },
//
//    COST_PER_DISTANCE_ONLY() {
//        @Override
//        public double annualFleetCosts(AnalysisSummary analysisSummary, RoboTaxiCostParameters cp) {
//            double annualDistance = analysisSummary.getDistanceElement().totalDistance * 365;
//            double Ckm = cp.getCostParameter(CostParameterIdentifiersAmodeus.COST_KM);
//            return Ckm * annualDistance;
//        }
//    }
//
//    ;
//    
//    @Override
//    public TotalValueIdentifier getTotalValueIdentifier() {
//        return TotalValueIdentifiersAmodeus.ANNUALFLEETCOST;
//    }
//
//}
