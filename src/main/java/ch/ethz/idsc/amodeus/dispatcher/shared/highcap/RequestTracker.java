/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.amodeus.dvrp.request.AVRequest;

/* package */ enum RequestTracker {
    ;
    public static Set<AVRequest> getNewAddedValidRequests(Set<AVRequest> openRequestSet, Set<AVRequest> lastValidRequestSet) {
        return openRequestSet.stream().filter(avRequest -> !lastValidRequestSet.contains(avRequest)).collect(Collectors.toSet());
    }

    public static Set<AVRequest> getRemovedRequests(Set<AVRequest> openRequestSet, Set<AVRequest> lastValidRequestSet) {
        return lastValidRequestSet.stream().filter(avRequest -> !openRequestSet.contains(avRequest)).collect(Collectors.toSet());
    }

    public static Set<AVRequest> getRemainedRequests(Set<AVRequest> openRequestSet, Set<AVRequest> lastValidRequestSet) {
        return lastValidRequestSet.stream().filter(openRequestSet::contains).collect(Collectors.toSet());
    }

    public static void removeClosedRequest(Set<AVRequest> requestPool, Collection<AVRequest> openRequests) {
        requestPool.removeIf(avRequest -> !openRequests.contains(avRequest));
    }

    /** this function remove overdue request in the request pool
     * 
     * @param requestPool
     * @param requestKeyInfoMap
     * @param now
     * @param requestMatchedLastTime
     * @return the set of removed requests */
    public static Set<AVRequest> removeOverduedRequest(Set<AVRequest> requestPool, //
            Map<AVRequest, RequestKeyInfo> requestKeyInfoMap, double now, Set<AVRequest> requestMatchedLastTime) {
        Set<AVRequest> overduedRequests = requestPool.stream().filter(avRequest -> //
        requestKeyInfoMap.get(avRequest).getDeadlinePickUp() < now && !requestMatchedLastTime.contains(avRequest)).collect(Collectors.toSet());
        requestPool.removeAll(overduedRequests);
        return overduedRequests;
    }
}
