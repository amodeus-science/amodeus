package ch.ethz.matsim.av.passenger;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Route;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.passenger.PassengerRequestCreator;

import ch.ethz.matsim.av.routing.AVRoute;

public class AVRequestCreator implements PassengerRequestCreator {
    private final Network network;
    private final String mode;

    public AVRequestCreator(String mode, Network network) {
        this.network = network;
        this.mode = mode;
    }

    @Override
    public PassengerRequest createRequest(Id<Request> id, Id<Person> passengerId, Route route, Link pickupLink, Link dropoffLink, double departureTime, double submissionTime) {
        if (pickupLink == null || dropoffLink == null) {
            throw new IllegalStateException("Pickup or dropoff link is null: " + pickupLink + " / " + dropoffLink);
        }

        AVRoute avRoute = (AVRoute) route;
        avRoute.setDistance(Double.NaN);

        Link operatorPickupLink = network.getLinks().get(pickupLink.getId());
        Link operatorDropoffLink = network.getLinks().get(dropoffLink.getId());

        if (operatorPickupLink == null) {
            throw new IllegalStateException(String.format("Pickup link does not exist in network of AMoDeus mode %s: %s", mode, pickupLink.getId()));
        }

        if (operatorDropoffLink == null) {
            throw new IllegalStateException(String.format("Dropoff link does not exist in network of AMoDeus mode %s: %s", mode, dropoffLink.getId()));
        }

        return new AVRequest(id, passengerId, operatorPickupLink, operatorDropoffLink, departureTime, submissionTime, avRoute, mode);
    }
}
