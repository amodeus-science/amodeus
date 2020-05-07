package ch.ethz.matsim.av.routing.interaction;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree;

public class InteractionLinkData {
    private final QuadTree<Link> index;

    public InteractionLinkData(Collection<Link> links) {
        Set<Node> nodes = new HashSet<>();
        nodes.addAll(links.stream().map(Link::getFromNode).collect(Collectors.toSet()));
        nodes.addAll(links.stream().map(Link::getToNode).collect(Collectors.toSet()));

        double[] bounds = NetworkUtils.getBoundingBox(nodes);
        this.index = new QuadTree<>(bounds[0], bounds[1], bounds[2], bounds[3]);

        links.stream().forEach(l -> index.put(l.getCoord().getX(), l.getCoord().getY(), l));
    }

    public Link getClosestLink(Coord coord) {
        return index.getClosest(coord.getX(), coord.getY());
    }

    public int getNumberOfLinks() {
        return index.size();
    }

    static public InteractionLinkData empty() {
        return new InteractionLinkData(Collections.emptySet());
    }

    static public InteractionLinkData fromAttribute(String attributeName, Network network) {
        Collection<Link> links = new LinkedList<>();

        for (Link link : network.getLinks().values()) {
            Boolean flag = (Boolean) link.getAttributes().getAttribute(attributeName);

            if (flag != null && flag) {
                links.add(link);
            }
        }

        return new InteractionLinkData(links);
    }
}
