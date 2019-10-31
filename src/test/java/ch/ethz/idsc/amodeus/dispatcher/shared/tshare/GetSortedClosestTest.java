/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.NavigableMap;
import java.util.TreeMap;

import junit.framework.TestCase;

public class GetSortedClosestTest extends TestCase {
    public void testSimple() {
        NavigableMap<Integer, String> map = new TreeMap<>();
        map.put(5, "e");
        map.put(2, "b");
        map.put(1, "a");
        map.put(3, "c");
        map.put(4, "d");

        try {
            GetSortedClosest.elem(0, map);
            fail();
        } catch (RuntimeException e) {
            // ---
        }

        try {
            GetSortedClosest.elem(map.size() + 1, map);
            fail();
        } catch (RuntimeException e) {
            // ---
        }

        assertEquals("abc", String.join("", GetSortedClosest.elem(3, map)));
    }
}
