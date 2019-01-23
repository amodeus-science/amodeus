package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ enum GetSortedClosest {
    ;

    /** @return first @param n elements of the {@link NavigableMap} @param map */
    public static <T, U> List<U> elem(int n, NavigableMap<T, U> map) {
        GlobalAssert.that(n >= 1);
        GlobalAssert.that(n <= map.size());
        List<U> closest = new ArrayList<>();
        Entry<T, U> entry = map.firstEntry();
        closest.add(entry.getValue());
        for (int i = 1; i < n; ++i) {
            Entry<T, U> next = map.higherEntry(entry.getKey());
            entry = next;
            closest.add(entry.getValue());
        }
        return closest;
    }

}
