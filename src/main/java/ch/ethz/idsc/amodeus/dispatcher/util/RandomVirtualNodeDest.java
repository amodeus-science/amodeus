/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.gbl.MatsimRandom;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNode;

public class RandomVirtualNodeDest implements AbstractVirtualNodeDest {

    @Override
    public List<Link> selectLinkSet(VirtualNode<Link> virtualNode, int size) {
        /** no selection */
        if (size < 1)
            return Collections.emptyList();

        List<Link> selected = new ArrayList<>();
        List<Link> links = virtualNode.getLinks().stream().collect(Collectors.toList());
        while (selected.size() < size) {
            int elemRand = MatsimRandom.getRandom().nextInt(links.size());
            Link randLink = links.stream().skip(elemRand).findFirst().get();
            selected.add(randLink);
        }
        GlobalAssert.that(selected.size() == size);
        return selected;
    }
}
