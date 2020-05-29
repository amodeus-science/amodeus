/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.List;
import java.util.Set;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;

public class RequestKeyInfo {
    private double deadlinePickUp;
    private double modifiableSubmissionTime;
    private double deadlineDropOff;
    private boolean allowanceGiven;

    public RequestKeyInfo(PassengerRequest avRequest, double maxWaitTime, double maxDelay, TravelTimeComputation ttc) {
        modifiableSubmissionTime = avRequest.getSubmissionTime();
        deadlinePickUp = avRequest.getSubmissionTime() + maxWaitTime;
        deadlineDropOff = avRequest.getSubmissionTime() + ttc.of(avRequest.getFromLink(), avRequest.getToLink(), //
                avRequest.getSubmissionTime(), true) + maxDelay;
        allowanceGiven = false;
    }

    public double getDeadlinePickUp() {
        return deadlinePickUp;
    }

    public double getDeadlineDropOff() {
        return deadlineDropOff;
    }

    public double getModifiableSubmissionTime() {
        return modifiableSubmissionTime;
    }

    public void modifyDeadlinePickUp(List<TripWithVehicle> lastAssignment, PassengerRequest avRequest, double maxWaitTime) {
        deadlinePickUp = modifiableSubmissionTime + maxWaitTime; // first write the original value
        // if it is assigned last time then make the deadline earlier
        for (TripWithVehicle tripWithVehicle : lastAssignment)
            if (tripWithVehicle.getTrip().contains(avRequest)) {
                for (StopInRoute stopInRoute : tripWithVehicle.getRoute()) {
                    if (stopInRoute.getavRequest() == avRequest && stopInRoute.getStopType() == SharedMealType.PICKUP) {
                        if (stopInRoute.getTime() < deadlinePickUp) // since we have added traffic allowance, we need to make sure this one
                            deadlinePickUp = stopInRoute.getTime();
                        break;
                    }
                }
                break;
            }
    }

    public void addTrafficAllowance(double trafficAllowance) {
        deadlinePickUp += trafficAllowance;
        deadlineDropOff += trafficAllowance;
        allowanceGiven = true;
    }

    public void removeTrafficAllowance(double trafficAllowance) { // this function is needed when we finish dealing with one roboTaxi, we need to change back
                                                                  // the deadlines
        if (allowanceGiven) {
            deadlinePickUp -= trafficAllowance;
            deadlineDropOff -= trafficAllowance;
            allowanceGiven = false;
        }
    }

    public void modifySubmissionTime(double now, double maxWaitTime, PassengerRequest avRequest, Set<PassengerRequest> overduedRequests) {
        if (modifiableSubmissionTime + maxWaitTime < now && overduedRequests.contains(avRequest)) {
            modifiableSubmissionTime += maxWaitTime;
            deadlinePickUp += maxWaitTime;
            deadlineDropOff += maxWaitTime;
            // note: this is due to we cannot reject request in this simulation, we assume the request is resubmitted after deadline is passed
            // limit the size of open request is strongly recommended
        }
    }
}
