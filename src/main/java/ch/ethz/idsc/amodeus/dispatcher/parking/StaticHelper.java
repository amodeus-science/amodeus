/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.parking;

import java.util.Collection;
import java.util.Random;

/* package */ enum StaticHelper {
    ;

    /** inspired by
     * https://stackoverflow.com/questions/21092086/get-random-element-from-collection
     * 
     * @param random
     * @param collection
     * @return */
    static <T> T randomElement(Collection<T> collection, Random random) {
        return collection.stream() //
                .skip(random.nextInt(collection.size())) //
                .findFirst().get();
    }

}
