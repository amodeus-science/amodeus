/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.virtualnetwork;

import java.util.Collection;
import java.util.function.Function;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.red.Mean;

/* package */ enum StaticHelper {
    ;

    public static <T> Tensor meanOf(Collection<T> elements, Function<T, Tensor> locationOf) {
        return Mean.of(Tensor.of(elements.stream().map(locationOf::apply)));
    }
}
