package ch.ethz.idsc.amodeus.linkspeed;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

public enum LinkIndex {
    ;

    // OLD
    // public static Integer fromLink(Link link) {
    // return Integer.parseInt(link.getId().toString());
    // }

    // WITH STRING
    public static String fromLink(Link link) {
        return link.getId().toString();
    }

    public static Id<Link> fromString(String string) {
        return Id.createLinkId(string);
    }
}
