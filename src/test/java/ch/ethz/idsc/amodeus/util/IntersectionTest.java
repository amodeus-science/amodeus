/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

public class IntersectionTest {

    @Test
    public void test() {

        List<Integer> c1 = Arrays.asList(1, 2, 3, 3);
        List<Integer> c2 = Arrays.asList(3, 4, 5);

        /** function is used in {@link DualSideSearch} */
        Collection<Integer> intersection = CollectionUtils.intersection(c1, c2);

        Assert.assertTrue(intersection.size() == 1);
        Assert.assertTrue(intersection.contains(3));

    }

}
