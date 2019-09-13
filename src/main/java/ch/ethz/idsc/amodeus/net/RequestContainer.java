/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;

/** the suffix "index" is chosen since the value is not identical to the "ID" of matsim
 * 
 * values are initialized to -1 to detect is assignment has been overlooked */
public class RequestContainer implements Serializable {

    /** WARNING:
     * 
     * ANY MODIFICATION IN THIS CLASS EXCEPT COMMENTS WILL INVALIDATE PREVIOUS
     * SIMULATION RECORDINGS
     * 
     * DO NOT MODIFY THIS CLASS UNLESS THERE IS A VERY GOOD REASON */

    /** these values are invariant for the entire life of the request */
    public int requestIndex = -1; // <- valid values are positive
    public double submissionTime = -1;
    public int fromLinkIndex = -1; // where the person is now
    public int toLinkIndex = -1; // where the person wants to go

    /** these values might change with time, the local history
     * is tracked for later processing */
    private NavigableMap<Long, RequestStatus> statusTrace = new TreeMap<>();
    // contains associated vehicles
    private NavigableMap<Long, Integer> vehicleMap = new TreeMap<>();

    /** @return last recorded {@link RequestStatus} */
    public RequestStatus getStatus() {
        if (statusTrace.isEmpty())
            return null;
        return statusTrace.lastEntry().getValue();
    }

    @Deprecated // TODO remove eventually
    public Map<Long, RequestStatus> getTrace() {
        return Collections.unmodifiableMap(statusTrace);
    }

    @Deprecated // TODO this will be removed.
    public Set<RequestStatus> allStatii() {
        Set<RequestStatus> all = new HashSet<>();
        statusTrace.values().forEach(s -> all.add(s));
        return all;
    }

    /** record {@link RequestStatus}: (@param time,@param status) */
    public void addStatus(Long time, RequestStatus status) {
        this.statusTrace.put(time, status);
    }

    /** @return last recorded associated vehicle */
    public Integer associatedVehicle() {
        if (vehicleMap.isEmpty())
            return null;
        return vehicleMap.lastEntry().getValue();
    }

    /** record {@link RequestStatus}: (@param time,@param status) */
    public void addAssociatedVehicle(Long time, Integer vehicleIndex) {
        this.vehicleMap.put(time, vehicleIndex);
    }

}
