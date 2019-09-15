/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.io.Serializable;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;

public class VehicleContainer implements Serializable {
    public static final int LINK_UNSPECIFIED = -1;

    /** WARNING:
     * 
     * ANY MODIFICATION IN THIS CLASS EXCEPT COMMENTS
     * WILL INVALIDATE PREVIOUS SIMULATION RECORDINGS
     * 
     * DO NOT MODIFY THIS CLASS UNLESS
     * THERE IS A VERY GOOD REASON */

    public int vehicleIndex = -1; // for tracking of individual vehicles

    // linkIndex is unrelated to MATSIM's LinkId, but is our own consecutive and memory efficient id
    /** value -1 in case no particular destination */
    public int linkIndex = LINK_UNSPECIFIED;

    public RoboTaxiStatus roboTaxiStatus = null;

    /** value -1 in case no particular destination */
    public int destinationLinkIndex = LINK_UNSPECIFIED;

//    public int getLinkId() {
//        return linkIndex;
//    }
}
