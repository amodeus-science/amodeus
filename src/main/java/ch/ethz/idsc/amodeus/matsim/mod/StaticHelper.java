/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.matsim.av.config.operator.GeneratorConfig;

// TODO Joel redundant to StaticHelper in ch.ethz.idsc.amodeus.dispatcher.util
/* package */ enum StaticHelper {
    ;
    /* package */ static List<Link> getSortedLinks(Collection<Link> links) {
        List<Link> sortedLinks = new ArrayList<>(links);
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
