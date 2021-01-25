package amodeus.amodeus.dispatcher.alonso_mora_2016;

public class AlonsoMoraParameters {
    public boolean updateActivePickupTime = false;
    public boolean updateActiveDropoffTime = false;

    public boolean useSoftConstraintsAfterAssignment = true;
    public double unassignedPenalty = 72000;

    public int rtvLimitPerVehicle = Integer.MAX_VALUE;
    public int rtvLimitPerFleet = Integer.MAX_VALUE;

    public int routeOptimizationLimit = Integer.MAX_VALUE;

    public int travelTimeCacheInterval = 1200;

    public enum RejectionType {
        FirstUnsuccessulAssignment, AfterInitialPickupTime, ResubmitAfterInitialPickupTime
    }

    public RejectionType rejectionType = RejectionType.FirstUnsuccessulAssignment;

    public enum RouteSearchType {
        Extensive, Euclidean
    }

    public RouteSearchType routeSearchType = RouteSearchType.Euclidean;

    public class ExtensiveSearchParameters {
        public boolean useDepthFirst = true;
        public int searchLimit = Integer.MAX_VALUE; // 10000;
    }

    public ExtensiveSearchParameters extensiveSearch = new ExtensiveSearchParameters();

    public class EuclideanSearchParameters {
        public boolean failEarly = true;
    }

    public EuclideanSearchParameters euclideanSearch = new EuclideanSearchParameters();
}
