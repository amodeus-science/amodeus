package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/*package */ class RequestRegister {

    private final Map<RoboTaxi, Map<String, AVRequest>> register = new HashMap<>();

    /** Adding and removing */
    /* package */ void add(RoboTaxi roboTaxi, AVRequest avRequest) {
        if (!register.containsKey(roboTaxi)) {
            register.put(roboTaxi, new HashMap<>());
        }
        register.get(roboTaxi).put(avRequest.getId().toString(), avRequest);
    }

    /* package */ void remove(RoboTaxi roboTaxi, AVRequest avRequest) {
        GlobalAssert.that(register.containsKey(roboTaxi));
        GlobalAssert.that(register.get(roboTaxi).containsKey(avRequest.getId().toString()));
        AVRequest val = register.get(roboTaxi).remove(avRequest.getId().toString());
        Objects.requireNonNull(val);
        if (register.get(roboTaxi).isEmpty()) {
            Map<String, AVRequest> val2 = register.remove(roboTaxi);
            Objects.requireNonNull(val2);
        }
    }

    /** contains functions */
    /* package */ boolean contains(RoboTaxi roboTaxi) {
        return register.containsKey(roboTaxi);
    }

    /* package */ boolean contains(AVRequest avRequest) {
        return getAssignedAvRequests().contains(avRequest);
    }

    /* package */ boolean contains(RoboTaxi roboTaxi, AVRequest avRequest) {
        if (!contains(roboTaxi)) {
            return false;
        }
        if (!contains(avRequest)) {
            return false;
        }
        return true;
    }

    /** Get Functions */

    /* package */ Set<AVRequest> getAssignedAvRequests() {
        // TODO improve
        Set<AVRequest> avRequests = new HashSet<>();
        for (Map<String, AVRequest> avRequestsMap : register.values()) {
            avRequests.addAll(avRequestsMap.values());
        }
        return avRequests;
    }

    /* package */ Optional<RoboTaxi> getAssignedRoboTaxi(AVRequest avRequest) {
        for (Entry<RoboTaxi, Map<String, AVRequest>> requestRegisterEntry : register.entrySet()) {
            if (requestRegisterEntry.getValue().containsKey(avRequest.getId().toString())) {
                return Optional.of(requestRegisterEntry.getKey());
            }
        }
        System.out.println("Check... Here we should not go");
        GlobalAssert.that(false);
        return Optional.ofNullable(null);
    }

    /* package */ Map<AVRequest, RoboTaxi> getPickupRegister(Set<AVRequest> pendingRequests) {
        Map<AVRequest, RoboTaxi> pickupRegister = new HashMap<>();
        for (Entry<RoboTaxi, Map<String, AVRequest>> requestRegisterEntry : register.entrySet()) {
            for (AVRequest avRequest : requestRegisterEntry.getValue().values()) {
                if (pendingRequests.contains(avRequest)) {
                    GlobalAssert.that(!pickupRegister.containsKey(avRequest)); // In that case some of the logic failed. every request can only be assigned to one vehicle
                    pickupRegister.put(avRequest, requestRegisterEntry.getKey());
                }
            }
        }
        return pickupRegister;
    }

    /* package */ Map<String, AVRequest> get(RoboTaxi roboTaxi) {
        GlobalAssert.that(contains(roboTaxi));
        return register.get(roboTaxi);
    }

    /* package */ Map<RoboTaxi, Map<String, AVRequest>> getRegister() {
        return Collections.unmodifiableMap(register);
    }

}
