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

<<<<<<< HEAD
    private NavigableMap<Long, Integer> linkTrace = new TreeMap<>();
    private NavigableMap<Long, RoboTaxiStatus> statusTrace = new TreeMap<>();
    private NavigableMap<Long, Integer> destTrace = new TreeMap<>();

    /** @return linkIndex (not MATSim's {@link Id<Link>} of last recorded
     *         location, -1 if no data present. */
    public int getLastLinkIndex() {
        if (linkTrace.isEmpty())
            return LINK_UNSPECIFIED;
        return linkTrace.lastEntry().getValue();
    }
=======
    /** Links the RoboTaxi traveled on ordered w.r.t time, the integer
     * linkIndex is unrelated to MATSIM's LinkId, but is our own consecutive
     * and memory efficient id, the value -1 is set in case the information is
     * not available */
    public int[] linkTrace = new int[] { LINK_UNSPECIFIED };
>>>>>>> master

    /** record known link location: (@param time,@param linkIndex) */
    public void addLinkLocation(Long time, Integer linkIndex) {
        this.linkTrace.put(time, linkIndex);
    }

    /** @return last recorded {@link RoboTaxiStatus} */
    public RoboTaxiStatus getLastStatus() {
        if (statusTrace.isEmpty())
            return null;
        return statusTrace.lastEntry().getValue();
    }

<<<<<<< HEAD
    /** record {@link RoboTaxiStatus}: (@param time,@param status) */
    public void addStatus(Long time, RoboTaxiStatus status) {
        this.statusTrace.put(time, status);
    }

    /** @return linkIndex (not MATSim's {@link Id<Link>} of last recorded
     *         destination, -1 if no data present. */
    public int getLastDest() {
        if (destTrace.isEmpty())
            return LINK_UNSPECIFIED;
        return destTrace.lastEntry().getValue();
    }

    /** record known destination: (@param time,@param linkIndex) */
    public void addDestination(Long time, Integer linkIndex) {
        this.destTrace.put(time, linkIndex);
    }

=======
>>>>>>> master
}
