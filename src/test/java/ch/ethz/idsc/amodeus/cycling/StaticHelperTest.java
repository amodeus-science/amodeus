/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.cycling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class StaticHelperTest extends TestCase {

    public void test() {
        {
            List<Integer> testList = new ArrayList<>();
            testList.add(5308);
            testList.add(5307);
            testList.add(5307);
            testList.add(5307);
            testList.add(5308);
            List<Integer> checkList = Arrays.asList(5308, 5307, 5308);
            System.err.println(testList);
            StaticHelper.removeDuplicates(testList);
            for (int i = 0; i < testList.size(); ++i)
                assertEquals(checkList.get(i), testList.get(i), 0);
            System.err.println(testList);
        }
        {
            List<Integer> testList = Arrays.asList(5308, 5307, 5307, 5307, 5308);
            List<Integer> shortened = StaticHelper.removeDuplicatesCopy(testList);
            System.out.println(testList);
            System.out.println(shortened);
            assertEquals(5308, (int) shortened.get(0));
            assertEquals(5307, (int) shortened.get(1));
            assertEquals(5308, (int) shortened.get(2));
        }
        {
            List<Integer> testList = new ArrayList<>();
            testList.add(1);
            testList.add(2);
            testList.add(3);
            testList.add(3);
            testList.add(3);
            testList.add(3);
            testList.add(3);
            testList.add(4);
            testList.add(4);
            testList.add(4);
            testList.add(4);
            testList.add(5);
            testList.add(4);
            testList.add(4);
            testList.add(4);
            List<Integer> shortened = StaticHelper.removeDuplicatesCopy(testList);
            assertEquals(1, shortened.get(0), 0);
            assertEquals(2, shortened.get(1), 0);
            assertEquals(3, shortened.get(2), 0);
            assertEquals(4, shortened.get(3), 0);
            assertEquals(5, shortened.get(4), 0);
            assertEquals(4, shortened.get(5), 0);
            System.out.println(testList);
            System.out.println(shortened);
        }
        {
            List<String> testList = new ArrayList<>();
            testList.add("a");
            testList.add("a");
            testList.add("a");
            testList.add("b");
            testList.add("b");
            testList.add("b");
            testList.add("c");
            testList.add("b");
            testList.add("b");
            List<String> shortened = StaticHelper.removeDuplicatesCopy(testList);

            System.out.println(testList);
            System.out.println(shortened);

            assertEquals("a", shortened.get(0));
            assertEquals("b", shortened.get(1));
            assertEquals("c", shortened.get(2));
            assertEquals("b", shortened.get(3));
        }
        {
            assertFalse(StaticHelper.containsMultiples(Arrays.asList(1, 2, 3)));
            assertTrue(StaticHelper.containsMultiples(Arrays.asList(1, 2, 2, 3)));
            assertTrue(StaticHelper.containsMultiples(Arrays.asList(1, 2, 3, 2)));
        }
    }
}
