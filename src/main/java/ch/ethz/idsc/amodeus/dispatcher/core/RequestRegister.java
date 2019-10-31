/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/*package */ class RequestRegister {

    private final Map<RoboTaxi, Map<String, AVRequest>> register = new HashMap<>();

    /** Adding and removing */
    /* package */ void add(RoboTaxi roboTaxi, AVRequest avRequest) {
        register.putIfAbsent(roboTaxi, new HashMap<>());
        register.get(roboTaxi).put(avRequest.getId().toString(), avRequest);
    }

    /* package */ void remove(RoboTaxi roboTaxi, AVRequest avRequest) {
        GlobalAssert.that(register.containsKey(roboTaxi));
        GlobalAssert.that(register.get(roboTaxi).containsKey(avRequest.getId().toString()));
        Objects.requireNonNull(register.get(roboTaxi).remove(avRequest.getId().toString()));
        if (register.get(roboTaxi).isEmpty())
            Objects.requireNonNull(register.remove(roboTaxi));
    }

    /* package */ void remove(RoboTaxi roboTaxi) {
        GlobalAssert.that(register.containsKey(roboTaxi));
        Objects.requireNonNull(register.remove(roboTaxi));
    }

    /** contains functions */
    /* package */ boolean contains(RoboTaxi roboTaxi) {
        return register.containsKey(roboTaxi);
    }

    /* package */ boolean contains(AVRequest avRequest) {
        return getAssignedAvRequests().contains(avRequest);
    }

    /* package */ boolean contains(RoboTaxi roboTaxi, AVRequest avRequest) {
        return contains(roboTaxi) && contains(avRequest);
    }

    /** Get Functions */

    /* package */ Set<AVRequest> getAssignedAvRequests() {
        Set<AVRequest> avRequests = new HashSet<>();
        for (Map<String, AVRequest> avRequestsMap : register.values())
            avRequests.addAll(avRequestsMap.values());

        return avRequests;
    }

    /* package */ Optional<RoboTaxi> getAssignedRoboTaxi(AVRequest avRequest) {
        for (Entry<RoboTaxi, Map<String, AVRequest>> requestRegisterEntry : register.entrySet())
            if (requestRegisterEntry.getValue().containsKey(avRequest.getId().toString()))
                return Optional.of(requestRegisterEntry.getKey());

        throw new RuntimeException("no can do");
    }

    /* package */ Map<AVRequest, RoboTaxi> getPickupRegister(Set<AVRequest> pendingRequests) {
        Map<AVRequest, RoboTaxi> pickupRegister = new HashMap<>();
        for (Entry<RoboTaxi, Map<String, AVRequest>> requestRegisterEntry : register.entrySet())
            for (AVRequest avRequest : requestRegisterEntry.getValue().values())
                if (pendingRequests.contains(avRequest)) {
                    GlobalAssert.that(!pickupRegister.containsKey(avRequest)); // In that case some of the logic failed. every request can only be assigned to
                                                                               // one vehicle
                    pickupRegister.put(avRequest, requestRegisterEntry.getKey());
                }

        return pickupRegister;
    }

    /* package */ Set<AVRequest> getAssignedPendingRequests(Set<AVRequest> pendingRequests) {
        return getAssignedAvRequests().stream().filter(pendingRequests::contains).collect(Collectors.toSet());
    }

    /* package */ Map<String, AVRequest> get(RoboTaxi roboTaxi) {
        GlobalAssert.that(contains(roboTaxi));
        return register.get(roboTaxi);
    }

    /* package */ Map<RoboTaxi, Map<String, AVRequest>> getRegister() {
        return Collections.unmodifiableMap(register);
    }
}
