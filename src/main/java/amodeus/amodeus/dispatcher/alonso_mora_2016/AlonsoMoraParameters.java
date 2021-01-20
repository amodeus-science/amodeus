package amodeus.amodeus.dispatcher.alonso_mora_2016;

public class AlonsoMoraParameters {
    public boolean useActivePickupTime;
    public boolean useSoftConstraintsAfterAssignment;
    public double unassignedPenalty = 1e9;
    
    public int rtvLimitPerVehicle = 10000;
    public int rtvLimitPerFleet = Integer.MAX_VALUE;
    
    public boolean useEuclideanTripProposals = true;
}
