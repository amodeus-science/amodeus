/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

import java.util.HashSet;
import java.util.Set;

public enum TotalValueIdentifiersAmodeus implements TotalValueIdentifier {

    // General, Dispatchers, Generators
    TIMESTAMP("timeStamp"), //
    DISPATCHER("dispatcher"), //
    VEHICLEGENERATOR("vehicleGenerator"), //
    DISTANCEHEURISTIC("distanceHeuristic"), //
    REBALANCEPERIOD("rebalancePeriod"), //
    DISPATCHINGPERIOD("dispatchingPeriod"), //
    VIRTUALNODES("virtualNodes"), //
    TOTALVEHICLES("totalVehicles"), //
    TOTALREQUESTS("totalRequests"), //
    POPULATIONSIZE("populationSize"), //

    // Wait Times
    MEANWAITINGTIME("MeanWaitingTime"), //
    WAITTIMEQUANTILE1("WaitTimeQuantile1"), //
    WAITTIMEQUANTILE2("WaitTimeQuantile2"), //
    WAITTIMEQUANTILE3("WaitTimeQuantile3"), //
    WAITTIMEMAX("waitTimeMax"), //

    // Drive Times
    // MEANDRIVETIME("MeanDriveTime"), //
    // TOTALROBOTAXIDRIVETIME("TotalRoboTaxiDriveTime"), //

    // Distances
    TOTALROBOTAXIDISTANCE("TotalRoboTaxiDistance"), //
    TOTALROBOTAXIDISTANCEREB("totalRoboTaxiDistanceReb"), //
    TOTALROBOTAXIDISTANCEPICKU("totalRoboTaxiDistancePicku"), //
    TOTALROBOTAXIDISTANCEWTCST("totalRoboTaxiDistanceWtCst"), //
    AVGTRIPDISTANCE("averageTripDistance"), //
    DISTANCERATIO("distanceRatio"), //
    OCCUPANCYRATIO("occupancyRatio"), //

    // Velocities
    // AVERAGEROBOTAXIVELOCITY("AverageRobotaxiVelocity"), //

    // COSTS
    ANNUALFLEETCOST("annualFleetCost"), //
    ;

    private final String identifier;

    private TotalValueIdentifiersAmodeus(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }
    
    public static Set<String> getAllIdentifiers() {
        Set<String> allIdentifiers = new HashSet<>();
        for (TotalValueIdentifiersAmodeus totalValueIdentifiersAmodeus : values()) {
            allIdentifiers.add(totalValueIdentifiersAmodeus.identifier);
        }
        return allIdentifiers;
    }
}
