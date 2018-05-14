/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.nd;

import java.io.Serializable;

import ch.ethz.idsc.tensor.Tensor;

class NdPair<V> implements Serializable {
    final Tensor location; // <- key
    private final V value;

    /* package */ NdPair(Tensor location, V value) {
        this.location = location.unmodifiable();
        this.value = value;
    }

    public V value() {
        return value;
    }
}