package ch.ethz.idsc.amodeus.linkspeed;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;

public enum LinkIndex {
    ;

    // OLD
    // public static Integer fromLink(Link link) {
    // return Integer.parseInt(link.getId().toString());
    // }

    // // WITH STRING
    // public static String fromLink(Link link) {
    // return link.getId().toString();
    // }
    //
    // // WITH STRING
    // public static Id<Link> fromString(String string) {
    // return Id.createLinkId(string);
    // }

    // WITH DB INDEX
    public static Integer fromLink(MatsimAmodeusDatabase db, Link link) {
        return db.getLinkIndex(link);
    }

    // WITH DB INDEX
    public static Id<Link> fromString(MatsimAmodeusDatabase db, Integer index) {
        return db.getOsmLink(index).link.getId();
    }

}
