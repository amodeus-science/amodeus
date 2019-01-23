package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ enum RequestTracker {
    ;
    static Set<AVRequest> getNewAddedValidRequests(Set<AVRequest> openRequestSet, Set<AVRequest> lastValidRequestSet) {
        Set<AVRequest> newAddedValidRequests = new HashSet<>();
        for (AVRequest avRequest : openRequestSet)
            if (!lastValidRequestSet.contains(avRequest))
                newAddedValidRequests.add(avRequest);
        return newAddedValidRequests;
    }

    static Set<AVRequest> getRemovedRequests(Set<AVRequest> openRequestSet, Set<AVRequest> lastValidRequestSet) {
        Set<AVRequest> removedRequests = new HashSet<>();
        for (AVRequest avRequest : lastValidRequestSet)
            if (!openRequestSet.contains(avRequest))
                removedRequests.add(avRequest);
        return removedRequests;
    }

    static Set<AVRequest> getRemainedRequests(Set<AVRequest> openRequestSet, Set<AVRequest> lastValidRequestSet) {
        Set<AVRequest> remainedRequests = new HashSet<>();
        for (AVRequest avRequest : lastValidRequestSet)
            if (openRequestSet.contains(avRequest))
                remainedRequests.add(avRequest);
        return remainedRequests;
    }

    static void removeClosedRequest(Set<AVRequest> requestPool, Collection<AVRequest> openRequests) {
        Set<AVRequest> closedRequests = new HashSet<>();
        for (AVRequest avRequest : requestPool)
            if (!openRequests.contains(avRequest))
                closedRequests.add(avRequest);
        requestPool.removeAll(closedRequests);
    }

    /** this function remove overdue request in the request pool
     * 
     * @param requestPool
     * @param requestKeyInfoMap
     * @param now
     * @param requestMatchedLastTime
     * @return the set of removed requests */

    static Set<AVRequest> removeOverduedRequest(Set<AVRequest> requestPool, //

            Map<AVRequest, RequestKeyInfo> requestKeyInfoMap, double now, Set<AVRequest> requestMatchedLastTime) {
        Set<AVRequest> overduedRequests = new HashSet<>();
        for (AVRequest avRequest : requestPool)
            if (requestKeyInfoMap.get(avRequest).getDeadlinePickUp() < now && !requestMatchedLastTime.contains(avRequest))
                overduedRequests.add(avRequest);
        requestPool.removeAll(overduedRequests);
        return overduedRequests;
    }
}
