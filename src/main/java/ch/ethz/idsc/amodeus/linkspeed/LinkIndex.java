package ch.ethz.idsc.amodeus.linkspeed;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;

// TODO eventually remove, was introduced to ensure that all link indexes
// are encoded in the same way.
public enum LinkIndex {
    ;

    public static Integer fromLink(MatsimAmodeusDatabase db, Link link) {
        return db.getLinkIndex(link);
    }

    public static Id<Link> fromString(MatsimAmodeusDatabase db, Integer index) {
        return db.getOsmLink(index).link.getId();
    }

}
