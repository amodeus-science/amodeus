/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.ArrayList;
import java.util.List;

/* package */ enum RebalancePlanGenerator {
    ;
    /** RebalancingPlanGenerator.of will generate a list of RebalanceTripWithVehicle. It intakes listOfAllRebalanceTripWithVehicle. The
     * way we assign the re-balancing vehicle is according to the paper (see the dispatcher). We do not use LP since we have found other
     * way to reach the same goal. */
    // TODO need to clarify some questions with the author of the paper, something may need to be changed

    public static List<RebalanceTripWithVehicle> of(List<RebalanceTripWithVehicle> listOfAllRebalanceTripWithVehicle) {
        List<RebalanceTripWithVehicle> rebalancePlan = new ArrayList<>();

        while (!listOfAllRebalanceTripWithVehicle.isEmpty()) {
            // find the re-balance trip with shortest time
            double minTime = listOfAllRebalanceTripWithVehicle.get(0).getTimeToTravel();
            int indexOfMinTime = 0;
            for (int i = 1; i < listOfAllRebalanceTripWithVehicle.size(); i++)
                if (listOfAllRebalanceTripWithVehicle.get(i).getTimeToTravel() < minTime) {
                    minTime = listOfAllRebalanceTripWithVehicle.get(i).getTimeToTravel();
                    indexOfMinTime = i;
                }
            // write that trip into the output list
            RebalanceTripWithVehicle rebalanceTripWithVehicle = listOfAllRebalanceTripWithVehicle.get(indexOfMinTime);
            rebalancePlan.add(rebalanceTripWithVehicle);

            // remove any other entry with the same roboTaxi or same request
            listOfAllRebalanceTripWithVehicle.removeIf(trip -> //
            trip.getAvRequest() == rebalanceTripWithVehicle.getAvRequest() || trip.getRoboTaxi() == rebalanceTripWithVehicle.getRoboTaxi());
        }
        return rebalancePlan;
    }
}
