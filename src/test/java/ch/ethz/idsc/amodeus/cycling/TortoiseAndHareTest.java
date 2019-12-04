/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.cycling;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class TortoiseAndHareTest extends TestCase {

    public void test() {
        {
            System.out.println("+++++++");
            System.out.println("TEST 6:");
            List<Integer> list = Arrays.asList(27290, 27289, 27291, 27287, 20769, 20770, 27288, 27287, 20769, 20771, 53466, 53467, 20772, 20770, 27288);
            boolean hasCycle = TortoiseAndHare.apply(list).hasNonTrivialCycle();
            assertTrue(!hasCycle);
            System.out.println(hasCycle);
        }
        {
            System.out.println("+++++++");
            System.out.println("TEST 5:");
            List<Integer> list = Arrays.asList(5308, 5307, 5307, 5307, 5308);
            boolean hasCycle = TortoiseAndHare.apply(list).hasNonTrivialCycle();
            assertTrue(!hasCycle);
            System.out.println(hasCycle);
        }
        {
            System.out.println("+++++++");
            System.out.println("TEST 0:");
            List<Integer> list = Arrays.asList(30278, 30396, 30396, 30394, 30392, 30428, 30428, 30428, 30428, 30428, 30428, 30428);
            boolean hasCycle = TortoiseAndHare.apply(list).hasNonTrivialCycle();
            System.out.println(hasCycle);
            assertTrue(!hasCycle);

        }
        {
            System.out.println("+++++++");
            System.out.println("TEST 1:");
            List<Integer> list = Arrays.asList(2, 0, 6, 3, 1, 6, 3, 1, 6, 3, 1);
            boolean hasCycle = TortoiseAndHare.apply(list).hasNonTrivialCycle();
            System.out.println(hasCycle);
            assertTrue(hasCycle);

        }
        {
            System.out.println("+++++++");
            System.out.println("TEST 2:");
            List<Integer> list = Arrays.asList(2, 0, 6, 3, 1, 6, 3, 1, 7, 7, 7, 7, 7, 7);
            boolean hasCycle = TortoiseAndHare.apply(list).hasNonTrivialCycle();
            assertTrue(!hasCycle);
            System.out.println(hasCycle);
        }
        {
            System.out.println("+++++++");
            System.out.println("TEST 3:");
            List<String> list = Arrays.asList("a", "b", "a", "b", "a", "b", "a", "b");
            boolean hasCycle = TortoiseAndHare.apply(list).hasNonTrivialCycle();
            assertTrue(hasCycle);
            System.out.println(hasCycle);
        }
        {
            System.out.println("+++++++");
            System.out.println("TEST 4:");
            List<String> list = Arrays.asList("a", "b", "a", "b", "a", "c", "a", "b");
            boolean hasCycle = TortoiseAndHare.apply(list).hasNonTrivialCycle();
            assertTrue(!hasCycle);
            System.out.println(hasCycle);
        }
    }
}
