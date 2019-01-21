/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import org.matsim.api.core.v01.network.Link;

@FunctionalInterface
public interface TaxiTrafficData {
    /** Reads out velocity of given link at given time
     * 
     * @param link
     * @param now
     * @return velocity on {@link Link} at time now */
    double getTravelTimeData(Link link, double now);

}
