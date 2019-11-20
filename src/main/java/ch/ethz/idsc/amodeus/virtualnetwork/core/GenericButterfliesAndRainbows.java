/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.idsc.amodeus.util.math.IntPoint;

/** class helps to detect adjacency between VirtualNodes based on a topological criterion. For every
 * VirtualNode a list of elements is supplied, if an element is contained in two virtualNodes, they
 * share a virtual Link.
 * 
 * class may be useful in other projects and therefore is made public */
public class GenericButterfliesAndRainbows<T, U> {
    private final Map<U, Set<VirtualNode<T>>> map = new HashMap<>();

    public void add(U node, VirtualNode<T> virtualNode) {
        map.computeIfAbsent(node, n -> new HashSet<>()) //
                /* map.get(node) */ .add(virtualNode);
    }

    public Collection<IntPoint> allPairs() {
        // some algorithms assume that an edge is adjacent in the list to the opposite edge
        Collection<IntPoint> collection = new LinkedHashSet<>();
        for (Set<VirtualNode<T>> set : map.values()) {
            if (1 < set.size()) {
                if (set.size() != 2)
                    System.out.println("Node shared by " + set.size() + " VirtualNodes");
                List<VirtualNode<T>> some = new ArrayList<>(set);
                for (int i = 0; i < some.size() - 1; ++i)
                    for (int j = i + 1; j < some.size(); ++j) {
                        collection.add(new IntPoint(some.get(i).getIndex(), some.get(j).getIndex()));
                        collection.add(new IntPoint(some.get(j).getIndex(), some.get(i).getIndex()));
                    }
            }
        }
        return collection;
    }
}
