/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.io.Serializable;
import java.util.NavigableMap;
import java.util.TreeMap;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;

public class VehicleContainer implements Serializable {
    public static final int LINK_UNSPECIFIED = -1;

    /** WARNING:
     * 
     * ANY MODIFICATION IN THIS CLASS EXCEPT COMMENTS
     * WILL INVALIDATE PREVIOUS SIMULATION RECORDINGS
     * 
     * DO NOT MODIFY THIS CLASS UNLESS
     * THERE IS A VERY GOOD REASON
     * 
     * IMPORTANT:
     * 
     * The used linkIndex are UNRELATED to MATSIM's LinkId,
     * but is our own consecutive and memory efficient id, translation
     * is done via the {@link MatsimAmodeusDatabase} */

    public int vehicleIndex = -1; // for tracking of individual vehicles

    private NavigableMap<Long, Integer> linkTrace = new TreeMap<>();
    private NavigableMap<Long, RoboTaxiStatus> statusTrace = new TreeMap<>();

    /** value -1 in case no particular destination */
    public int destinationLinkIndex = LINK_UNSPECIFIED;

    /** @return linkIndex (not MATSim's {@link Id<Link>} of last recorded
     *         location, -1 if no data present. */
    public int getLastLinkIndex() {
        if (linkTrace.isEmpty())
            return LINK_UNSPECIFIED;
        return linkTrace.lastEntry().getValue();
    }

    /** record known link location: (@param time,@param linkIndex) */
    public void addLinkLocation(Long time, Integer linkIndex) {
        this.linkTrace.put(time, linkIndex);
    }



    public RoboTaxiStatus getLastStatus() {
        if (statusTrace.isEmpty())
            return null;
        return statusTrace.lastEntry().getValue();
    }

    public void addStatus(Long time, RoboTaxiStatus status) {
        this.statusTrace.put(time, status);
    }

//    public RoboTaxiStatus roboTaxiStatus = null;

}
