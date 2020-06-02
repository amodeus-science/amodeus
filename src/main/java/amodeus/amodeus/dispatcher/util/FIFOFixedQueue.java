/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/** FIFO Queue with limited size, for 1 excess element added, the oldest element
 * is removed automatically. Can deliver the latest n added elements as a {@link List}
 *
 * @author clruch
 *
 * @param <T> */
public class FIFOFixedQueue<T> {
    private final int maxLength;
    private final LinkedList<T> list = new LinkedList<>();

    public FIFOFixedQueue(int maxLength) {
        this.maxLength = maxLength;
        System.out.println("max Length: " + maxLength);
    }

    public void manage(T t) {
        if (list.size() >= maxLength)
            list.removeLast();
        list.addFirst(t);
    }

    /** @return last
     * @param n elements added to the list */
    public List<T> getNewest(int n) {
        return new ArrayList<>(list.subList(0, Math.min(list.size(), n)));
    }
}
