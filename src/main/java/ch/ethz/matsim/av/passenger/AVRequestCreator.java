package ch.ethz.matsim.av.passenger;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Route;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.passenger.PassengerRequestCreator;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.routing.AVRoute;

@Singleton
public class AVRequestCreator implements PassengerRequestCreator {
	@Inject
	Map<Id<AVOperator>, AVOperator> operators;

	@Inject
	Map<Id<AVOperator>, AVDispatcher> dispatchers;

	@Inject
	Map<Id<AVOperator>, Network> networks;

	@Override
	public PassengerRequest createRequest(Id<Request> id, Id<Person> passengerId, Route route, Link pickupLink,
			Link dropoffLink, double departureTime, double submissionTime) {
		if (pickupLink == null || dropoffLink == null) {
			throw new IllegalStateException("Pickup or dropoff link is null: " + pickupLink + " / " + dropoffLink);
		}

		AVRoute avRoute = (AVRoute) route;
		avRoute.setDistance(Double.NaN);

		AVOperator operator = operators.get(avRoute.getOperatorId());

		if (operator == null) {
			throw new IllegalStateException("Operator '" + avRoute.getOperatorId().toString() + "' does not exist.");
		}

		Network network = networks.get(avRoute.getOperatorId());
		Link operatorPickupLink = network.getLinks().get(pickupLink.getId());
		Link operatorDropoffLink = network.getLinks().get(dropoffLink.getId());

		if (operatorPickupLink == null) {
			throw new IllegalStateException(String.format("Pickup link does not exist in network of operator %s: %s",
					operator.getId(), pickupLink.getId()));
		}

		if (operatorDropoffLink == null) {
			throw new IllegalStateException(String.format("Dropoff link does not exist in network of operator %s: %s",
					operator.getId(), dropoffLink.getId()));
		}

		return new AVRequest(id, passengerId, operatorPickupLink, operatorDropoffLink, departureTime, submissionTime,
				avRoute, operator, dispatchers.get(avRoute.getOperatorId()));
	}
}
