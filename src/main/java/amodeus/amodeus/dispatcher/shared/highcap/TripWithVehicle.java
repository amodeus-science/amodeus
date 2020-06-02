/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.highcap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.util.math.GlobalAssert;

public class TripWithVehicle {
    private final Set<PassengerRequest> trip = new HashSet<>();
    private final RoboTaxi roboTaxi;
    private final double totalDelay;
    private final List<StopInRoute> route;

    public TripWithVehicle(RoboTaxi roboTaxi, double totalDelay, Collection<PassengerRequest> trip, List<StopInRoute> route) {
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

    public Set<PassengerRequest> getTrip() {
        return trip;
    }

    public List<StopInRoute> getRoute() {
        GlobalAssert.that(!route.isEmpty());
        return route;
    }

}
