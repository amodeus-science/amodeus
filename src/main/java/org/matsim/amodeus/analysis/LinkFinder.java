package org.matsim.amodeus.analysis;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

public class LinkFinder {
    private final Network network;

    public LinkFinder(Network network) {
        this.network = network;
    }

    public Link getLink(Id<Link> linkId) {
        Link link = network.getLinks().get(linkId);

        if (link == null) {
            throw new IllegalStateException("Cannot find link: " + linkId);
        }

        return link;
    }

    public double getDistance(Id<Link> linkId) {
        return getLink(linkId).getLength();
    }
}
