/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.ArrayList;
import java.util.Iterator;
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
            list.removeFirst();
        list.add(t);
    }

    /** @return last
     * @param n elements added to the list */
    public List<T> getNewest(int n) {
        List<T> returnList = new ArrayList<>();
        Iterator<T> it = this.list.descendingIterator();
        int count = 0;
        while (it.hasNext() && count < n && count < list.size()) {
            returnList.add(it.next());
            ++count;
        }
        return returnList;
    }
}
