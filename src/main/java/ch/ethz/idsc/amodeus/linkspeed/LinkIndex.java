package ch.ethz.idsc.amodeus.linkspeed;

import org.matsim.api.core.v01.network.Link;

public enum LinkIndex {
    ;

    public static Integer fromLink(Link link) {
        return Integer.parseInt(link.getId().toString());
    }

}
