/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;

public class RandomVirtualNodeDest implements AbstractVirtualNodeDest {
    private static final long DEFAULT_RANDOM_SEED = 4711;
    // ---
    private final Random random;

    public RandomVirtualNodeDest() {
        random = new Random(DEFAULT_RANDOM_SEED);
    }

    @Override
    public List<Link> selectLinkSet(VirtualNode<Link> virtualNode, int size) {
        /** no selection */
        if (size < 1)
            return Collections.emptyList();

        List<Link> selected = new ArrayList<>();
        List<Link> links = StaticHelper.getSortedLinks(virtualNode.getLinks());
        while (selected.size() < size) {
            int elemRand = random.nextInt(links.size());
            Link randLink = links.stream().skip(elemRand).findFirst().get();
            selected.add(randLink);
        }
        GlobalAssert.that(selected.size() == size);
        return selected;
    }
}
