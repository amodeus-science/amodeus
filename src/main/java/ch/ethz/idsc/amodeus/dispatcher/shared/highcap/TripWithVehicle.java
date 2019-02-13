/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

public class TripWithVehicle {
    private final Set<AVRequest> trip = new HashSet<>();
    private final RoboTaxi roboTaxi;
    private final double totalDelay;
    private final List<StopInRoute> route;

    public TripWithVehicle(RoboTaxi roboTaxi, double totalDelay, Collection<AVRequest> trip, List<StopInRoute> route) {
        this.roboTaxi = roboTaxi;
        this.totalDelay = totalDelay;
        this.trip.addAll(trip);
        GlobalAssert.that(!route.isEmpty());
        this.route = Collections.unmodifiableList(new ArrayList<>(route));
    }

    public RoboTaxi getRoboTaxi() {
        return roboTaxi;
    }

    public double getTotalDelay() {
        return totalDelay;
    }

    public Set<AVRequest> getTrip() {
        return trip;
    }

    public List<StopInRoute> getRoute() {
        GlobalAssert.that(!route.isEmpty());
        return route;
    }

}
