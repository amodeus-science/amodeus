package ch.ethz.matsim.av.waiting_time.dynamic;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

public class LinkGroupDefinition {
    private final int maximumIndex;
    private final Map<Id<Link>, Integer> indices;

    public LinkGroupDefinition(int maximumIndex, Map<Id<Link>, Integer> indices) {
        this.maximumIndex = maximumIndex;
        this.indices = indices;
    }

    public Collection<Id<Link>> getLinkIds(int index) {
        if (index > maximumIndex) {
            throw new IllegalAccessError();
        }

        Set<Id<Link>> linkIds = new HashSet<>();

        for (Map.Entry<Id<Link>, Integer> entry : indices.entrySet()) {
            if (entry.getValue() == index) {
                linkIds.add(entry.getKey());
            }
        }

        return linkIds;
    }

    public int getGroup(Id<Link> linkId) {
        return indices.getOrDefault(linkId, -1);
    }

    public int getMaximumIndex() {
        return maximumIndex;
    }

    static public LinkGroupDefinition create(Network network, String attribute) {
        Map<Id<Link>, Integer> indices = new HashMap<>();
        int maximumIndex = 0;

        for (Link link : network.getLinks().values()) {
            Integer groupIndex = (Integer) link.getAttributes().getAttribute(attribute);

            if (groupIndex != null) {
                indices.put(link.getId(), groupIndex);
                maximumIndex = Math.max(maximumIndex, groupIndex);
            }
        }

        return new LinkGroupDefinition(maximumIndex, indices);
    }
}
