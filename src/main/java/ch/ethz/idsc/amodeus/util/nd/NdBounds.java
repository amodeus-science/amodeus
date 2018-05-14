/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.util.nd;

import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.sca.Clip;

/* package */ class NdBounds {
    public final Tensor lBounds;
    public final Tensor uBounds;

    public NdBounds(Tensor lBounds, Tensor uBounds) {
        this.lBounds = lBounds.copy();
        this.uBounds = uBounds.copy();
    }

    public Scalar median(int index) {
        return lBounds.Get(index).add(uBounds.Get(index)).multiply(RationalScalar.HALF);
    }

    public Clip clip(int index) {
        return Clip.function(lBounds.Get(index), uBounds.Get(index));
    }
}
