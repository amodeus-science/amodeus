/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import org.matsim.api.core.v01.network.Link;

public interface TaxiTrafficData {

    /** @param link
     * @param now
     * @return velocity on {@link Link} at time now */
    double getTravelTimeData(Link link, double now);

    /** Create a TravelTimeDataArray using the linkSpeedData.bin file */
    void createTravelTimeData();
}
