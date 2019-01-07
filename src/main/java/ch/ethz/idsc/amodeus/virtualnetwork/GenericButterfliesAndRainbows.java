/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/** class helps to detect adjacency between VirtualNodes based on a topological criterion. For every
 * VirtualNode a list of elements is supplied, if an element is contained in two virtualNodes, they
 * share a virtual Link.
 * 
 * class may be useful in other projects and therefore is made public */
public class GenericButterfliesAndRainbows<T, U> {
    private final Map<U, Set<VirtualNode<T>>> map = new HashMap<>();

    public void add(U node, VirtualNode<T> virtualNode) {
        if (!map.containsKey(node))
            map.put(node, new HashSet<>());
        map.get(node).add(virtualNode);
    }

    public List<Point> allPairs() {
        // some algorithms assume that an edge is adjacent in the list to the opposite edge
        List<Point> list = new LinkedList<>();
        for (Entry<U, Set<VirtualNode<T>>> entry : map.entrySet()) {
            Set<VirtualNode<T>> set = entry.getValue();
            if (1 < set.size()) {
                if (set.size() != 2)
                    System.out.println("Node shared by " + set.size() + " VirtualNodes");
                List<VirtualNode<T>> some = new ArrayList<>(set);
                for (int i = 0; i < some.size() - 1; ++i)
                    for (int j = i + 1; j < some.size(); ++j) {
                        list.add(new Point(some.get(i).getIndex(), some.get(j).getIndex()));
                        list.add(new Point(some.get(j).getIndex(), some.get(i).getIndex()));
                    }
            }
        }

        // ensure that there are no duplicate pairs
        List<Point> uniqueList = new LinkedList<>();
        for (Point point : list) {
            boolean noduplicate = true;
            for (Point uniqueP : uniqueList)
                if (point.equals(uniqueP)) {
                    noduplicate = false;
                    break;
                }
            if (noduplicate)
                uniqueList.add(point);
        }
        return uniqueList;
    }
}
