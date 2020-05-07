package ch.ethz.matsim.av.passenger;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.routing.AVRoute;
import ch.ethz.refactoring.schedule.AmodeusDropoffTask;
import ch.ethz.refactoring.schedule.AmodeusPickupTask;

public class AVRequest implements PassengerRequest {
    final private Id<Request> id;
    final private double submissionTime;

    final private Link pickupLink;
    final private Link dropoffLink;
    final private Id<Person> passengerId;
    final private AVRoute route;

    private AmodeusPickupTask pickupTask;
    private AmodeusDropoffTask dropoffTask;

    private final String mode;
    private final Id<AVOperator> operatorId;

    // TODO: Can we remove Dispatcher and Operator from here?
    // And the tasks.
    public AVRequest(Id<Request> id, Id<Person> passengerId, Link pickupLink, Link dropoffLink, double pickupTime, double submissionTime, AVRoute route, String mode,
            Id<AVOperator> operatorId) {
        this.id = id;
        this.passengerId = passengerId;
        this.pickupLink = pickupLink;
        this.dropoffLink = dropoffLink;
        this.route = route;
        this.submissionTime = submissionTime;
        this.mode = mode;
        this.operatorId = operatorId;
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

    public AmodeusPickupTask getPickupTask() {
        return pickupTask;
    }

    public void setPickupTask(AmodeusPickupTask pickupTask) {
        this.pickupTask = pickupTask;
    }

    public AmodeusDropoffTask getDropoffTask() {
        return dropoffTask;
    }

    public void setDropoffTask(AmodeusDropoffTask dropoffTask) {
        this.dropoffTask = dropoffTask;
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
        return mode;
    }

    public Id<AVOperator> getOperatorId() {
        return operatorId;
    }
}
