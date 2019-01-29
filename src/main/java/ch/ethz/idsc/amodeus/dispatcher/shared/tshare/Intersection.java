/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.ArrayList;
import java.util.Collection;

/* package */ enum Intersection {
    ;

    /** @return {@link Collection} of elements which are contained
     *         in @param c1 and @param c2 */
    public static <T> Collection<T> of(Collection<T> c1, Collection<T> c2) {
        Collection<T> intersection = new ArrayList<>();
        c1.stream().forEach(t -> {
            if (c2.contains(t))
                intersection.add(t);
        });
        return intersection;
    }

}
