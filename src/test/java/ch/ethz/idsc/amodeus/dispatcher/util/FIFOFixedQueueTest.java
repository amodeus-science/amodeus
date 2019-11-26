/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import junit.framework.TestCase;

public class FIFOFixedQueueTest extends TestCase {

    public void test() {
        int size = 3;

        FIFOFixedQueue<Integer> queue = new FIFOFixedQueue<>(size);

        assertEquals(queue.getNewest(100).size(), 0, 0.0);

        queue.manage(1);
        assertEquals(queue.getNewest(1).get(0), 1, 0.0);

        queue.manage(2);
        assertEquals(queue.getNewest(1).get(0), 2, 0.0);
        assertEquals(queue.getNewest(1).size(), 1, 0.0);
        assertEquals(queue.getNewest(2).get(1), 1, 0.0);

        queue.manage(3);
        assertEquals(queue.getNewest(3).get(0), 3, 0.0);
        assertEquals(queue.getNewest(3).get(1), 2, 0.0);
        assertEquals(queue.getNewest(3).get(2), 1, 0.0);
        assertEquals(queue.getNewest(1).get(0), 3, 0.0);

        queue.manage(4);
        assertEquals(queue.getNewest(3).get(0), 4, 0.0);
        assertEquals(queue.getNewest(3).get(1), 3, 0.0);
        assertEquals(queue.getNewest(3).get(2), 2, 0.0);
        assertEquals(queue.getNewest(1).get(0), 4, 0.0);
    }
}
