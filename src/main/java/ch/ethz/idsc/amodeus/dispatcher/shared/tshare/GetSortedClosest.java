package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;

/* package */ enum GetSortedClosest {
    ;

    public static List<VirtualNode<Link>> elem(int n, NavigableMap<Double, VirtualNode<Link>> map) {
        GlobalAssert.that(n >= 1);
        GlobalAssert.that(n <= map.size());
        List<VirtualNode<Link>> closest = new ArrayList<>();
        Entry<Double, VirtualNode<Link>> entry = map.firstEntry();
        closest.add(entry.getValue());
        for (int i = 1; i < n; ++i) {
            Entry<Double, VirtualNode<Link>> next = map.higherEntry(entry.getKey());
            entry = next;
            closest.add(entry.getValue());
        }
        return closest;
    }


}
