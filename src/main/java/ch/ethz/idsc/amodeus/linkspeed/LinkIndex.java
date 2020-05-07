/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.linkspeed;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;

@Deprecated
public enum LinkIndex {
    ;

    /** @deprecated use {@link Id#get(int, Class)} and {@link Id#index()} instead */
    @Deprecated
    public static Integer fromLink(MatsimAmodeusDatabase db, Link link) {
        return db.getLinkIndex(link);
    }

    /** @deprecated use {@link Id#get(int, Class)} and {@link Link#getId()} instead */
    @Deprecated
    public static Id<Link> fromString(MatsimAmodeusDatabase db, Integer index) {
        return db.getOsmLink(index).link.getId();
    }

}
