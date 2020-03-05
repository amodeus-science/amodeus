/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMealType;
import ch.ethz.idsc.amodeus.dispatcher.util.TreeMultipleItems;
import ch.ethz.idsc.amodeus.routing.NetworkTimeDistInterface;
import ch.ethz.matsim.av.passenger.AVRequest;

/** A {@link RequestHandler} takes care of all the requests in the scenario. It allows to quickly access the
 * desired subgroups such as unassigned Requests or allows to find the earliest requests computationally efficient. */
/* package */ class RequestHandler {
    /** Structure for the Track of Wait times and so on */
    private final Map<AVRequest, RequestWrap> requests = new HashMap<>();
    /** unnassigned Requests Sorted in a Navigable Map such that the earliest Request can be found easily */
    private final TreeMultipleItems<AVRequest> unassignedRequests = new TreeMultipleItems<>(r -> r.getSubmissionTime());
    /** All requests submitted in the last hour. This is used for the Rebalancing. */
    private final TreeMultipleItems<AVRequest> requestsLastHour = new TreeMultipleItems<>(r -> r.getSubmissionTime());
    /** All the drive Times for each requests if this request would be served directely with a unit capacity robo taxi */
    private final Map<AVRequest, Double> driveTimesSingle = new HashMap<>();
    /** All the effective Pickupe Times for each Requests. needed for the Constraints */
    private final Map<AVRequest, Double> pickupTimes = new HashMap<>(); // TODO never queried
    /** All the Requests which were Pending in the last time Step */
    private final Set<AVRequest> lastStepPending = new HashSet<>();

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

    public void addUnassignedRequests(Collection<AVRequest> unassignedAVRequests, NetworkTimeDistInterface timeDb, Double now) {
        unassignedAVRequests.forEach(r -> {
            unassignedRequests.add(r);
            requestsLastHour.add(r);
            driveTimesSingle.put(r, timeDb.travelTime(r.getFromLink(), r.getToLink(), now).number().doubleValue());
        });

        unassignedAVRequests.stream().filter(avr -> !requests.containsKey(avr)).forEach(avr -> requests.put(avr, new RequestWrap(avr)));
        unassignedAVRequests.forEach(avr -> requests.get(avr).setUnitCapDriveTime(timeDb.travelTime(avr.getFromLink(), avr.getToLink(), now).number().doubleValue()));
    }

    public void updatePickupTimes(Collection<AVRequest> avRequests, double now) {
        lastStepPending.stream().filter(r -> !avRequests.contains(r)).forEach(r -> requests.get(r).setPickupTime(now));
        lastStepPending.stream().filter(r -> !avRequests.contains(r)).forEach(r -> pickupTimes.put(r, now));
        lastStepPending.clear();
        lastStepPending.addAll(avRequests);
    }

    public void updateLastHourRequests(double now, double binsizetraveldemand) {
        requestsLastHour.removeAllElementsWithValueSmaller(now - binsizetraveldemand);
    }

    public Set<Link> getRequestLinksLastHour() {
        return requestsLastHour.getValues().stream().map(AVRequest::getFromLink).collect(Collectors.toSet());
    }

    public void removeFromUnasignedRequests(AVRequest avRequest) {
        unassignedRequests.remove(avRequest);
    }

    public void removeUnassignedRequestsBefore(double submissionTime) {
        unassignedRequests.removeAllElementsWithValueSmaller(submissionTime);
    }

    public List<AVRequest> getInOrderOffSubmissionTime() {
        return unassignedRequests.getTsInOrderOfValue();
    }

    public boolean isOnWaitList(AVRequest avRequest) {
        return requests.get(avRequest).isOnWaitList();
    }

    public void addToWaitList(AVRequest avRequest) {
        requests.get(avRequest).putToWaitList();
    }

    public void addToExtreemWaitList(AVRequest avRequest) {
        requests.get(avRequest).putToExtreemWaitList();
    }

    public double getDriveTimeDirectUnitCap(AVRequest avRequest) {
        return driveTimesSingle.get(avRequest);
    }

    public Map<AVRequest, Double> getDriveTimes(SharedAvRoute route) {
        // Preparation
        Map<AVRequest, Double> thisPickupTimes = new HashMap<>();
        route.getRoute().stream() //
                .filter(srp -> srp.getMealType().equals(SharedMealType.PICKUP)) //
                .forEach(srp -> thisPickupTimes.put(srp.getAvRequest(), srp.getArrivalTime()));
        //
        Map<AVRequest, Double> driveTimes = new HashMap<>();
        for (SharedRoutePoint sharedRoutePoint : route.getRoute())
            if (sharedRoutePoint.getMealType().equals(SharedMealType.DROPOFF))
                if (thisPickupTimes.containsKey(sharedRoutePoint.getAvRequest()))
                    // TODO does it include the dropoff or not?
                    driveTimes.put(sharedRoutePoint.getAvRequest(), sharedRoutePoint.getEndTime() - thisPickupTimes.get(sharedRoutePoint.getAvRequest()));
                else
                    driveTimes.put(sharedRoutePoint.getAvRequest(), //
                            sharedRoutePoint.getEndTime() - requests.get(sharedRoutePoint.getAvRequest()).getPickupTime());
        return driveTimes;
    }

    public RequestWrap getRequestWrap(AVRequest avRequest) {
        return requests.get(avRequest);
    }

    public double calculateWaitTime(AVRequest avRequest) {
        return extremWaitListTime//
                .filter(t -> requests.get(avRequest).isOnExtreemWaitList())//
                .orElse(calculateInternal(avRequest));
    }

    private double calculateInternal(AVRequest avRequest) {
        return (isOnWaitList(avRequest)) ? maxWaitTime : waitListTime;
    }

    public Set<AVRequest> getCopyOfUnassignedAVRequests() {
        return unassignedRequests.getValues();
    }

}