/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import org.matsim.api.core.v01.network.Link;

// TODO DEPLOY does this class have to be public? if yes, document purpose of class
public class LinkSpeedData {
    private final Link link;
    private final double[] travelTime;

    public LinkSpeedData(Link link, int slots) {
        this.link = link;
        this.travelTime = new double[slots];
    }

    public double getTravelTime(final int timeSlot, final double now) {
        if (travelTime[timeSlot] == 0.0) {
            return link.getLength() / link.getFreespeed(now);
        }
        return travelTime[timeSlot];
    }

    public void setTravelTime(int timeSlot, double value) {
        travelTime[timeSlot] = value;
    }
}
