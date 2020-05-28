/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.Objects;

import org.matsim.amodeus.dvrp.request.AVRequest;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/* package */ class RebalanceTripWithVehicle {
    private final AVRequest avRequest; // since there is only 1 request in each re-balnce trip, we don't need to use set
    private final RoboTaxi roboTaxi;
    private final double timeToTravel;

    public RebalanceTripWithVehicle(RoboTaxi roboTaxi, double timeToTravel, AVRequest avRequest) {
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

    public AVRequest getAvRequest() {
        return avRequest;
    }
}
