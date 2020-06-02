/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.highcap;

import java.util.Objects;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.core.RoboTaxi;

/* package */ class RebalanceTripWithVehicle {
    private final PassengerRequest avRequest; // since there is only 1 request in each re-balnce trip, we don't need to use set
    private final RoboTaxi roboTaxi;
    private final double timeToTravel;

    public RebalanceTripWithVehicle(RoboTaxi roboTaxi, double timeToTravel, PassengerRequest avRequest) {
        this.roboTaxi = Objects.requireNonNull(roboTaxi);
        this.timeToTravel = timeToTravel;
        this.avRequest = avRequest;
    }

    public RoboTaxi getRoboTaxi() {
        return roboTaxi;
    }

    public double getTimeToTravel() {
        return timeToTravel;
    }

    public PassengerRequest getAvRequest() {
        return avRequest;
    }
}
