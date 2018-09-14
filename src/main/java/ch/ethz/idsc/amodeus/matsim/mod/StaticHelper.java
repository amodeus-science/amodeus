package ch.ethz.idsc.amodeus.matsim.mod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.matsim.api.core.v01.network.Link;

public enum StaticHelper {
    ;
    /* package */ static ArrayList<Link> getSortedLinks(Collection<Link> links) {
        ArrayList<Link> sortedLinks = new ArrayList<Link>(links);
        Collections.sort(sortedLinks, LINK_COMPARATOR);
        return sortedLinks;
    }

    private static final Comparator<Link> LINK_COMPARATOR = new LinkComparator();

    private static class LinkComparator implements Comparator<Link> {
        @Override
        public int compare(Link o1, Link o2) {
            return o1.getId().toString().compareTo(o2.getId().toString());
        }
    }
}
