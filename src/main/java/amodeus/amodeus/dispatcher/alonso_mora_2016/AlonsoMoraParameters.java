package amodeus.amodeus.dispatcher.alonso_mora_2016;

public class AlonsoMoraParameters {
    public boolean updateActivePickupTime = false;
    public boolean updateActiveDropoffTime = false;

    public boolean useSoftConstraintsAfterAssignment = false;
    public double unassignedPenalty = 1e9;

    public int rtvLimitPerVehicle = 10000;
    public int rtvLimitPerFleet = Integer.MAX_VALUE;

    public int routeOptimizationLimit = Integer.MAX_VALUE;

    public enum RejectionType {
        FirstUnsuccessulAssignment, AfterInitialPickupTime, ResubmitAfterInitialPickupTime
    }

    public RejectionType rejectionType = RejectionType.ResubmitAfterInitialPickupTime;

    public enum RouteSearchType {
        Extensive, Euclidean
    }

    public RouteSearchType routeSearchType = RouteSearchType.Extensive;

    public class ExtensiveSearchParameters {
        public boolean useDepthFirst = true;
        public int searchLimit = 10000;
    }

    public ExtensiveSearchParameters extensiveSearch = new ExtensiveSearchParameters();

    public class EuclideanSearchParameters {
        public boolean failEarly = true;
    }

    public EuclideanSearchParameters euclideanSearch = new EuclideanSearchParameters();
}
