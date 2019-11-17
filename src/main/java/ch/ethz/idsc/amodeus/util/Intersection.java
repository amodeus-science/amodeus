/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util;

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

/** TODO depending on how duplicates should be treated could be replaced by {@link CollectionUtils#intersection}, see below */
public enum Intersection {
    ;

    /** @return {@link Collection} of elements which are contained
     *         in @param c1 and @param c2 */
    public static <T> Collection<T> of(Collection<T> c1, Collection<T> c2) {
        return c1.stream().filter(c2::contains).collect(Collectors.toList()); // has duplicates
        // return CollectionUtils.intersection(c1, c2); // no duplicates
    }
}
