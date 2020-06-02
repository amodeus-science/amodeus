/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.highcap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

/* package */ enum RequestTracker {
    ;
    public static Set<PassengerRequest> getNewAddedValidRequests(Set<PassengerRequest> openRequestSet, Set<PassengerRequest> lastValidRequestSet) {
        return openRequestSet.stream().filter(avRequest -> !lastValidRequestSet.contains(avRequest)).collect(Collectors.toSet());
    }

    public static Set<PassengerRequest> getRemovedRequests(Set<PassengerRequest> openRequestSet, Set<PassengerRequest> lastValidRequestSet) {
        return lastValidRequestSet.stream().filter(avRequest -> !openRequestSet.contains(avRequest)).collect(Collectors.toSet());
    }

    public static Set<PassengerRequest> getRemainedRequests(Set<PassengerRequest> openRequestSet, Set<PassengerRequest> lastValidRequestSet) {
        return lastValidRequestSet.stream().filter(openRequestSet::contains).collect(Collectors.toSet());
    }

    public static void removeClosedRequest(Set<PassengerRequest> requestPool, Collection<PassengerRequest> openRequests) {
        requestPool.removeIf(avRequest -> !openRequests.contains(avRequest));
    }

    /** this function remove overdue request in the request pool
     * 
     * @param requestPool
     * @param requestKeyInfoMap
     * @param now
     * @param requestMatchedLastTime
     * @return the set of removed requests */
    public static Set<PassengerRequest> removeOverduedRequest(Set<PassengerRequest> requestPool, //
            Map<PassengerRequest, RequestKeyInfo> requestKeyInfoMap, double now, Set<PassengerRequest> requestMatchedLastTime) {
        Set<PassengerRequest> overduedRequests = requestPool.stream().filter(avRequest -> //
        requestKeyInfoMap.get(avRequest).getDeadlinePickUp() < now && !requestMatchedLastTime.contains(avRequest)).collect(Collectors.toSet());
        requestPool.removeAll(overduedRequests);
        return overduedRequests;
    }
}
