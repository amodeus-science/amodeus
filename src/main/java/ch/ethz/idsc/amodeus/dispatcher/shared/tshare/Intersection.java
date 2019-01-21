package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.ArrayList;
import java.util.Collection;

/* package */ enum Intersection {
    ;

    public static <T> Collection<T> of(Collection<T> c1, Collection<T> c2) {
        Collection<T> intersection = new ArrayList<>();
        c1.stream().forEach(t -> {
            if (c2.contains(t))
                intersection.add(t);
        });
        return intersection;
    }

}
