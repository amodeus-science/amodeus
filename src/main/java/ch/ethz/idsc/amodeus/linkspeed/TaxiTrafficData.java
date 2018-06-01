/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import org.matsim.api.core.v01.network.Link;

public interface TaxiTrafficData {

    /** Reads out velocity of given link at given time
     * 
     * @param link
     * @param now
     * @return velocity on {@link Link} at time now */
    double getTravelTimeData(Link link, double now);

    /** Create a TravelTimeDataArray using the linkSpeedData.bin file
     * Loads the linkSpeedData.bin file and creates TravelTimeDataArray file for use
     * in MATSims TravelTimeCalculator */
    void createTravelTimeData();
}
