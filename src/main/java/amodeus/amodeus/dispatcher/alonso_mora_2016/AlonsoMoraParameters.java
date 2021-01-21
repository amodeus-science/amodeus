package amodeus.amodeus.dispatcher.alonso_mora_2016;

public class AlonsoMoraParameters {
    public boolean updateActivePickupTime = true;
    public boolean updateActiveDropoffTime = false;

    public boolean useSoftConstraintsAfterAssignment = true;
    public double unassignedPenalty = 1e9;

    public int rtvLimitPerVehicle = 10000;
    public int rtvLimitPerFleet = Integer.MAX_VALUE;

    public int routeOptimizationLimit = 10000;
    
    public boolean useEuclideanTripProposals = true;
}
