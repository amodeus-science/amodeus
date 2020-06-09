/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.cycling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/* package */ enum StaticHelper {
    ;

    /** @param list
     * @return {@link List} with all adjacent duplicate elements removed */
    public static <T> void removeDuplicates(List<T> list) {
        Iterator<T> iterator = list.iterator();
        T before = null;
        while (iterator.hasNext()) {
            T current = iterator.next();
            if (current.equals(before))
                iterator.remove();
            before = current;
        }
    }

    /** @param list
     * @return {@link List} with all adjacent duplicate elements removed */
    public static <T> List<T> removeDuplicatesCopy(List<T> list) {
        if (list.size() == 0)
            return list;
        T before = list.get(0);
        List<T> newList = new ArrayList<>(Collections.singletonList(before));
        for (T current : list.subList(1, list.size())) {
            if (!current.equals(before))
                newList.add(current);
            before = current;
        }
        return newList;
    }

    public static <T> boolean containsMultiples(List<T> list) {
        return new HashSet<>(list).size() != list.size();
    }
}
