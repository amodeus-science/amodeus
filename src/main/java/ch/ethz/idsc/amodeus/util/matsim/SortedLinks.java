/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.matsim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.matsim.api.core.v01.network.Link;

public enum SortedLinks {
    ;

    public static List<Link> of(Collection<Link> links) {
        List<Link> sortedLinks = new ArrayList<>(links);
        sortedLinks.sort(LINK_COMPARATOR);
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
