package org.matsim.amodeus.waiting_time.link_attribute;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

public class LinkWaitingTimeData {
    private final Map<Id<Link>, Double> waitingTimes;
    private final boolean isEmpty;

    LinkWaitingTimeData(Map<Id<Link>, Double> waitingTimes) {
        this.waitingTimes = waitingTimes;
        this.isEmpty = this.waitingTimes.size() == 0;
    }

    public double getWaitingTime(Id<Link> linkId, double defaultWaitingTime) {
        if (isEmpty) {
            return defaultWaitingTime;
        }

        return waitingTimes.getOrDefault(linkId, defaultWaitingTime);
    }

    static public LinkWaitingTimeData create(Network network, String linkAttribute) {
        Map<Id<Link>, Double> waitingTimes = new HashMap<>();

        for (Link link : network.getLinks().values()) {
            Double waitingTime = (Double) link.getAttributes().getAttribute(linkAttribute);

            if (waitingTime != null) {
                waitingTimes.put(link.getId(), waitingTime);
            }
        }

        return new LinkWaitingTimeData(waitingTimes);
    }

    static public LinkWaitingTimeData createEmpty() {
        return new LinkWaitingTimeData(Collections.emptyMap());
    }
}
