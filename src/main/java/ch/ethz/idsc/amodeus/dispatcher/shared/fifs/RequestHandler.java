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

import ch.ethz.idsc.amodeus.dispatcher.util.TreeMultipleItems;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */class RequestHandler {
    // Structure for the Track of Wait times and so on
    private final Map<AVRequest, RequestWrap> requests = new HashMap<>();
    // unnassigned Requests Sorted in a Navigable Map such that the earliest Request can be found easily
    private final TreeMultipleItems<AVRequest> unassignedRequests = new TreeMultipleItems<>(this::getSubmissionTime);
    // All requests submitted in the last hour. This is used for the Rebalancing.
    private final TreeMultipleItems<AVRequest> requestsLastHour = new TreeMultipleItems<>(this::getSubmissionTime);
    // All the drive Times for each requests if this request would be served directely with a unit capacity robo taxi
    private final Map<AVRequest, Double> driveTimesSingle = new HashMap<>();
    // All the effective Pickupe Times for each Requests. needed for the Constraints
    private final Map<AVRequest, Double> pickupTimes = new HashMap<>();
    // All the Requests which were Pending in the last time Step
    private final Set<AVRequest> lastStepPending = new HashSet<>();

    /** Time Constants for wait list times */
    private final Optional<Double> extremWaitListTime;
    private final double maxWaitTime;
    private final double waitListTime;

    /* package */ RequestHandler(double maxWaitTime, double waitListTime) {
        this.maxWaitTime = maxWaitTime;
        this.waitListTime = waitListTime;
        this.extremWaitListTime = Optional.empty();
    }

    /* package */ RequestHandler(double maxWaitTime, double waitListTime, Double extremWaitListTime) {
        this.extremWaitListTime = Optional.of(extremWaitListTime);
        this.maxWaitTime = maxWaitTime;
        this.waitListTime = waitListTime;
    }

    /* package */ void addUnassignedRequests(Collection<AVRequest> unassignedAVRequests, TravelTimeCalculatorCached timeDb) {
        unassignedAVRequests.stream().forEach(r -> {
            unassignedRequests.add(r);
            requestsLastHour.add(r);
            driveTimesSingle.put(r, timeDb.timeFromTo(r.getFromLink(), r.getToLink()).number().doubleValue());
        });

        unassignedAVRequests.stream().filter(avr -> !requests.containsKey(avr)).forEach(avr -> requests.put(avr, new RequestWrap(avr)));
        unassignedAVRequests.forEach(avr -> requests.get(avr).setUnitCapDriveTime(timeDb.timeFromTo(avr.getFromLink(), avr.getToLink()).number().doubleValue()));
    }

    /* package */ void updatePickupTimes(Collection<AVRequest> avRequests, double now) {
        lastStepPending.stream().filter(r -> !avRequests.contains(r)).forEach(r -> requests.get(r).setPickupTime(now));

        lastStepPending.stream().filter(r -> !avRequests.contains(r)).forEach(r -> pickupTimes.put(r, now));
        lastStepPending.clear();
        lastStepPending.addAll(avRequests);
    }

    /* package */ void updateLastHourRequests(double now, double binsizetraveldemand) {
        requestsLastHour.removeAllElementsWithValueSmaller(now - binsizetraveldemand);
    }

    /* package */ Set<Link> getRequestLinksLastHour() {
        return requestsLastHour.getValues().stream().map(avr -> avr.getFromLink()).collect(Collectors.toSet());
    }

    /* package */ void removeFromUnasignedRequests(AVRequest avRequest) {
        unassignedRequests.remove(avRequest);
    }

    /* package */ List<AVRequest> getInOrderOffSubmissionTime() {
        return unassignedRequests.getTsInOrderOfValue();
    }

    /* package */ boolean isOnWaitList(AVRequest avRequest) {
        return requests.get(avRequest).isOnWaitList();
    }

    /* package */ void addToWaitList(AVRequest avRequest) {
        requests.get(avRequest).putToWaitList();
    }

    /* package */ boolean isOnExtreemWaitList(AVRequest avRequest) {
        return requests.get(avRequest).isOnExtreemWaitList();
    }

    /* package */ void addToExtreemWaitList(AVRequest avRequest) {
        requests.get(avRequest).putToExtreemWaitList();
    }

    /* package */ double getDriveTimeDirectUnitCap(AVRequest avRequest) {
        return driveTimesSingle.get(avRequest);
    }

    /* package */ double getPickupTime(AVRequest avRequest) {
        return requests.get(avRequest).getPickupTime();
    }

    /* package */ RequestWrap getRequestWrap(AVRequest avRequest) {
        return requests.get(avRequest);
    }

    /* package */ double calculateWaitTime(AVRequest avRequest) {
        if (!extremWaitListTime.isPresent())
            return calculateInternal(avRequest);
        return (isOnExtreemWaitList(avRequest)) ? extremWaitListTime.get() : calculateInternal(avRequest);
    }

    private double calculateInternal(AVRequest avRequest) {
        return (isOnWaitList(avRequest)) ? maxWaitTime : waitListTime;
    }

    /* package */ Set<AVRequest> getCopyOfUnassignedAVRequests() {
        return unassignedRequests.getValues();
    }

    private Double getSubmissionTime(AVRequest request) {
        return request.getSubmissionTime();
    }

}
