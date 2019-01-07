/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import junit.framework.TestCase;

public class StaticHelperTest extends TestCase {

    public void testSimple() {
        Integer integer = StaticHelper.randomElement(Arrays.asList(6), new Random());
        assertEquals(integer.intValue(), 6);
    }

    public void testMore() {
        Random random = new Random();
        Set<Integer> set = IntStream.range(0, 100) //
                .mapToObj(i -> StaticHelper.randomElement(Arrays.asList(10, 11, 12), random)) //
                .collect(Collectors.toSet());
        assertEquals(set.size(), 3);
    }

    public void testSetMore() {
        Random random = new Random();
        Set<Integer> collection = new HashSet<>(Arrays.asList(10, 11, 12, 13));
        Set<Integer> set = IntStream.range(0, 100) //
                .mapToObj(i -> StaticHelper.randomElement(collection, random)) //
                .collect(Collectors.toSet());
        assertEquals(set.size(), 4);
    }

    public void testFail() {
        try {
            StaticHelper.randomElement(Collections.emptySet(), new Random());
            assertTrue(false);
        } catch (Exception exception) {
            // ---
        }
    }
}
