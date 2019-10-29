/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ enum RebalanceExplorer {
    ;
    /** the of function in RebalanceExplorer intakes unassigned vehicle and idling taxi, and generate a list of RebalanceTripWithVehicle.
     * It is similar to TripExploerer.of but for different purpose and much simpler. */
    public static List<RebalanceTripWithVehicle> of( //
            List<AVRequest> listOfUnassignedRequest, List<RoboTaxi> idlingRoboTaxis, //
            double now, TravelTimeComputation ttc) {
        List<RebalanceTripWithVehicle> listOfAllRebalanceTripWithVehicle = new ArrayList<>();
        for (RoboTaxi idlingRoboTaxi : idlingRoboTaxis)
            for (AVRequest unassignedRequest : listOfUnassignedRequest) {
                Link taxiLink = idlingRoboTaxi.getDivertableLocation();
                Link requestLink = unassignedRequest.getFromLink();
                double timeToTravel = ttc.of(taxiLink, requestLink, now, false); // do not store this, since rebalance does not take much time
                RebalanceTripWithVehicle thisRebalanceTrip = new RebalanceTripWithVehicle(idlingRoboTaxi, timeToTravel, unassignedRequest);
                listOfAllRebalanceTripWithVehicle.add(thisRebalanceTrip);
            }

        return listOfAllRebalanceTripWithVehicle;
    }
}
