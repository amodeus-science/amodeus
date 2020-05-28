package ch.ethz.matsim.av.passenger;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Route;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.passenger.PassengerRequestCreator;

import ch.ethz.matsim.av.routing.AVRoute;

public class AVRequestCreator implements PassengerRequestCreator {
    private final String mode;

    public AVRequestCreator(String mode) {
        this.mode = mode;
    }

    @Override
    public PassengerRequest createRequest(Id<Request> id, Id<Person> passengerId, Route route, Link pickupLink, Link dropoffLink, double departureTime, double submissionTime) {
        return new AVRequest(id, passengerId, pickupLink, dropoffLink, submissionTime, mode, (AVRoute) route);
    }
}
