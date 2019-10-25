/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;

public enum LinkIndex {
    ;

    public static Integer fromLink(MatsimAmodeusDatabase db, Link link) {
        return db.getLinkIndex(link);
    }

    public static Id<Link> fromString(MatsimAmodeusDatabase db, Integer index) {
        return db.getOsmLink(index).link.getId();
    }

}
