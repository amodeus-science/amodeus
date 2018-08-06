/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

// TODO JAN will move class to amodidsc when repos are in sync
/* package */ class MatchLinkObject<T> {
    private T t;
    private Link link;

    MatchLinkObject(T t) {
        GlobalAssert.that(t instanceof Link || t instanceof AVRequest);
        if (t instanceof Link) {
            this.link = (Link) t;
        }
        if (t instanceof AVRequest) {
            AVRequest avR = (AVRequest) t;
            this.link = avR.getFromLink();
        }
        this.t = t;
    }

    /* package */ Link getLink() {
        return link;
    }

    /* package */ T getObject() {
        return t;
    }
}