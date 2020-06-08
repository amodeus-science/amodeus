/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.fifs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.shared.SharedMealType;
import amodeus.amodeus.dispatcher.util.TreeMultipleItems;
import amodeus.amodeus.routing.NetworkTimeDistInterface;

/** A {@link RequestHandler} takes care of all the requests in the scenario. It allows to quickly access the
 * desired subgroups such as unassigned Requests or allows to find the earliest requests computationally efficient. */
/* package */ class RequestHandler {
    /** Structure for the Track of Wait times and so on */
    private final Map<PassengerRequest, RequestWrap> requests = new HashMap<>();
    /** unnassigned Requests Sorted in a Navigable Map such that the earliest Request can be found easily */
    private final TreeMultipleItems<PassengerRequest> unassignedRequests = new TreeMultipleItems<>(PassengerRequest::getSubmissionTime);
    /** All requests submitted in the last hour. This is used for the Rebalancing. */
    private final TreeMultipleItems<PassengerRequest> requestsLastHour = new TreeMultipleItems<>(PassengerRequest::getSubmissionTime);
    /** All the drive Times for each requests if this request would be served directely with a unit capacity robo taxi */
    private final Map<PassengerRequest, Double> driveTimesSingle = new HashMap<>();
    /** All the effective Pickupe Times for each Requests. needed for the Constraints */
    /** All the Requests which were Pending in the last time Step */
    private final Set<PassengerRequest> lastStepPending = new HashSet<>();

    /** Time Constants for wait list times */
    private final Optional<Double> extremWaitListTime;
    private final double maxWaitTime;
    private final double waitListTime;

    public RequestHandler(double maxWaitTime, double waitListTime) {
        this.maxWaitTime = maxWaitTime;
        this.waitListTime = waitListTime;
        this.extremWaitListTime = Optional.empty();
    }

    public RequestHandler(double maxWaitTime, double waitListTime, Double extremWaitListTime) {
        this.extremWaitListTime = Optional.of(extremWaitListTime);
        this.maxWaitTime = maxWaitTime;
        this.waitListTime = waitListTime;
    }

    public void addUnassignedRequests(Collection<PassengerRequest> unassignedPassengerRequests, NetworkTimeDistInterface timeDb, Double now) {
        unassignedPassengerRequests.forEach(r -> {
            unassignedRequests.add(r);
            requestsLastHour.add(r);
            driveTimesSingle.put(r, timeDb.travelTime(r.getFromLink(), r.getToLink(), now).number().doubleValue());
        });

        unassignedPassengerRequests.stream().filter(avr -> !requests.containsKey(avr)).forEach(avr -> requests.put(avr, new RequestWrap(avr)));
        unassignedPassengerRequests.forEach(avr -> requests.get(avr).setUnitCapDriveTime(timeDb.travelTime(avr.getFromLink(), avr.getToLink(), now).number().doubleValue()));
    }

    public void updatePickupTimes(Collection<PassengerRequest> avRequests, double now) {
        lastStepPending.stream().filter(r -> !avRequests.contains(r)).forEach(r -> requests.get(r).setPickupTime(now));
        lastStepPending.clear();
        lastStepPending.addAll(avRequests);
    }

    public void updateLastHourRequests(double now, double binsizetraveldemand) {
        requestsLastHour.removeAllElementsWithValueSmaller(now - binsizetraveldemand);
    }

    public Set<Link> getRequestLinksLastHour() {
        return requestsLastHour.getValues().stream().map(PassengerRequest::getFromLink).collect(Collectors.toSet());
    }

    public void removeFromUnasignedRequests(PassengerRequest avRequest) {
        unassignedRequests.remove(avRequest);
    }

    public List<PassengerRequest> getInOrderOffSubmissionTime() {
        return unassignedRequests.getTsInOrderOfValue();
    }

    public boolean isOnWaitList(PassengerRequest avRequest) {
        return requests.get(avRequest).isOnWaitList();
    }

    public void addToWaitList(PassengerRequest avRequest) {
        requests.get(avRequest).putToWaitList();
    }

    public void addToExtreemWaitList(PassengerRequest avRequest) {
        requests.get(avRequest).putToExtreemWaitList();
    }

    public double getDriveTimeDirectUnitCap(PassengerRequest avRequest) {
        return driveTimesSingle.get(avRequest);
    }

    public Map<PassengerRequest, Double> getDriveTimes(SharedAvRoute route) {
        // Preparation
        Map<PassengerRequest, Double> thisPickupTimes = new HashMap<>();
        route.getRoute().stream() //
                .filter(srp -> srp.getMealType().equals(SharedMealType.PICKUP)) //
                .forEach(srp -> thisPickupTimes.put(srp.getAvRequest(), srp.getArrivalTime()));
        //
        Map<PassengerRequest, Double> driveTimes = new HashMap<>();
        for (SharedRoutePoint sharedRoutePoint : route.getRoute())
            if (sharedRoutePoint.getMealType().equals(SharedMealType.DROPOFF))
                if (thisPickupTimes.containsKey(sharedRoutePoint.getAvRequest()))
                    // TODO @ChengQi does it include the dropoff or not?
                    driveTimes.put(sharedRoutePoint.getAvRequest(), sharedRoutePoint.getEndTime() - thisPickupTimes.get(sharedRoutePoint.getAvRequest()));
                else
                    driveTimes.put(sharedRoutePoint.getAvRequest(), //
                            sharedRoutePoint.getEndTime() - requests.get(sharedRoutePoint.getAvRequest()).getPickupTime());
        return driveTimes;
    }

    public RequestWrap getRequestWrap(PassengerRequest avRequest) {
        return requests.get(avRequest);
    }

    public double calculateWaitTime(PassengerRequest avRequest) {
        return extremWaitListTime//
                .filter(t -> requests.get(avRequest).isOnExtreemWaitList())//
                .orElse(calculateInternal(avRequest));
    }

    private double calculateInternal(PassengerRequest avRequest) {
        return (isOnWaitList(avRequest)) ? maxWaitTime : waitListTime;
    }

    public Set<PassengerRequest> getCopyOfUnassignedPassengerRequests() {
        return unassignedRequests.getValues();
    }

}