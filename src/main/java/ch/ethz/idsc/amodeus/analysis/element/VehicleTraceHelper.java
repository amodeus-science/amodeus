/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.element;

import java.util.Map.Entry;

import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.Scalar;

/* package */ enum VehicleTraceHelper {
    ;

    /** @return simply a linear interpolation, TODO sometime replace with a function from
     *         the tensor library...
     * @param time
     * @param pairLow
     * @param pairHigh */
    public static Scalar distanceAt(Long time, Entry<Long, Scalar> pairLow, Entry<Long, Scalar> pairHigh) {
        long dt = pairHigh.getKey() - pairLow.getKey();
        if (dt == 0)
            return pairLow.getValue().zero();
        Scalar dD = pairHigh.getValue().subtract(pairLow.getValue());
        Scalar dAdd = dD.multiply(RationalScalar.of(time - pairLow.getKey(), dt));
        return pairLow.getValue().add(dAdd);
    }

}
