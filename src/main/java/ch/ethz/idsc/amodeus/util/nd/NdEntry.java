/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.nd;

import java.io.Serializable;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

/** code by Eric Simonton adapted by jph and clruch */
public class NdEntry<V> implements Serializable {
    private final NdPair<V> ndPair;
    private final Scalar distance;

    /* package */ NdEntry(NdPair<V> ndPair, Scalar distance) {
        this.ndPair = ndPair;
        this.distance = distance;
    }

    /** @return location of pair in map */
    public Tensor location() {
        return ndPair.location;
    }

    /** @return value of pair in map */
    public V value() {
        return ndPair.value();
    }

    /** @return distance of pair to center */
    public Scalar distance() {
        return distance;
    }
}