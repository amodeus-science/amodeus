/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.highcap;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.core.RoboTaxi;

/* package */ enum RebalanceExplorer {
    ;
    /** the of function in RebalanceExplorer intakes unassigned vehicle and idling taxi, and generate a list of RebalanceTripWithVehicle.
     * It is similar to TripExploerer.of but for different purpose and much simpler. */
    public static List<RebalanceTripWithVehicle> of( //
            List<PassengerRequest> listOfUnassignedRequest, List<RoboTaxi> idlingRoboTaxis, //
            double now, TravelTimeComputation ttc) {
        List<RebalanceTripWithVehicle> listOfAllRebalanceTripWithVehicle = new ArrayList<>();
        for (RoboTaxi idlingRoboTaxi : idlingRoboTaxis)
            for (PassengerRequest unassignedRequest : listOfUnassignedRequest) {
                Link taxiLink = idlingRoboTaxi.getDivertableLocation();
                Link requestLink = unassignedRequest.getFromLink();
                double timeToTravel = ttc.of(taxiLink, requestLink, now, false); // do not store this, since rebalance does not take much time
                RebalanceTripWithVehicle thisRebalanceTrip = new RebalanceTripWithVehicle(idlingRoboTaxi, timeToTravel, unassignedRequest);
                listOfAllRebalanceTripWithVehicle.add(thisRebalanceTrip);
            }

        return listOfAllRebalanceTripWithVehicle;
    }
}
