/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import org.matsim.api.core.v01.network.Link;

public class LinkStatusPair {
    public final Link link;
    public final RoboTaxiStatus roboTaxiStatus;

    public LinkStatusPair(Link link, RoboTaxiStatus roboTaxiStatus) {
        this.link = link;
        this.roboTaxiStatus = roboTaxiStatus;
    }
}
