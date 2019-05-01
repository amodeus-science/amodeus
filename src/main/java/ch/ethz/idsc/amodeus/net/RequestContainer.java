/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.io.Serializable;
import java.util.Set;

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

    public int requestIndex = -1; // <- valid values are positive
    public int fromLinkIndex = -1; // where the person is now
    public double submissionTime = -1;
    public int toLinkIndex = -1; // where the person wants to go
    public Set<RequestStatus> requestStatus; // union of all statii held by the request in time interval of recording
    public int associatedVehicle; // vehicle currently associated to request, e.g., assigned for pickup
}
