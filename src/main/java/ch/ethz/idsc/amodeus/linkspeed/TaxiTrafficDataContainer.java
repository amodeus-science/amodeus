/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import java.util.Map;
import java.util.TreeMap;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.trafficmonitoring.TravelTimeData;

class TaxiTrafficDataContainer {
    private final int numSlots;
    private final Map<Id<Link>, TravelTimeData> trafficData = new TreeMap<>();

    public TaxiTrafficDataContainer(int numSlots) {
        this.numSlots = numSlots;
    }

    public double getLinkTravelTime(Link link, double now) {
        if (trafficData.containsKey(link.getId()))
            return trafficData.get(link.getId()).getTravelTime(getTimeSlot(now), now);
        return link.getLength() / link.getFreespeed(now);
    }

    public void addData(Id<Link> linkID, TravelTimeData ttData) {
        trafficData.put(linkID, ttData);
    }

    // TODO fix this function to something more nice.
    public int getTimeSlot(double time) {
        if (time > 0.0) {
            int slot = (int) (((time / StaticHelper.DAYLENGTH) * numSlots) - 1);
            if (slot <= numSlots - 1)
                return slot;
            return numSlots - 1;
        }
        return 0;
    }
}