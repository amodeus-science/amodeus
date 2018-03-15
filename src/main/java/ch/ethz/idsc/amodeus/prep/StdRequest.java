/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.prep;

import org.matsim.api.core.v01.network.Link;

/* package */ class StdRequest {
    public final Link ante;
    public double departureTime;
    public Link post;

    public StdRequest(Link from) {
        this.ante = from;
    }
}
