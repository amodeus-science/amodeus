package ch.ethz.matsim.av.passenger;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.routing.AVRoute;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;

public class AVRequest implements PassengerRequest {
	final private Id<Request> id;
	final private double submissionTime;

	final private Link pickupLink;
	final private Link dropoffLink;
	final private Id<Person> passengerId;
	final private AVOperator operator;
	final private AVDispatcher dispatcher;
	final private AVRoute route;

	private AVPickupTask pickupTask;
	private AVDropoffTask dropoffTask;

	public AVRequest(Id<Request> id, Id<Person> passengerId, Link pickupLink, Link dropoffLink, double pickupTime,
			double submissionTime, AVRoute route, AVOperator operator, AVDispatcher dispatcher) {
		this.id = id;
		this.passengerId = passengerId;
		this.pickupLink = pickupLink;
		this.dropoffLink = dropoffLink;
		this.operator = operator;
		this.route = route;
		this.dispatcher = dispatcher;
		this.submissionTime = submissionTime;
	}

	@Override
	public Link getFromLink() {
		return pickupLink;
	}

	@Override
	public Link getToLink() {
		return dropoffLink;
	}

	@Override
	public Id<Person> getPassengerId() {
		return passengerId;
	}

	public AVPickupTask getPickupTask() {
		return pickupTask;
	}

	public void setPickupTask(AVPickupTask pickupTask) {
		this.pickupTask = pickupTask;
	}

	public AVDropoffTask getDropoffTask() {
		return dropoffTask;
	}

	public void setDropoffTask(AVDropoffTask dropoffTask) {
		this.dropoffTask = dropoffTask;
	}

	public AVOperator getOperator() {
		return operator;
	}

	public AVDispatcher getDispatcher() {
		return dispatcher;
	}

	public AVRoute getRoute() {
		return route;
	}

	@Override
	public double getSubmissionTime() {
		return submissionTime;
	}

	@Override
	public Id<Request> getId() {
		return id;
	}

	@Override
	public double getEarliestStartTime() {
		return 0.0;
	}

	@Override
	public String getMode() {
		return AVModule.AV_MODE;
	}
}
