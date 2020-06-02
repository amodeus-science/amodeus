/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.util.math;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Ordering;
import ch.ethz.idsc.tensor.red.Tally;

/** {@link Ordering} */
public enum Ranking {
    ;
    /** @param vector
     * @return */
    public static Tensor of(Tensor vector) {
        NavigableMap<Tensor, Long> navigableMap = Tally.sorted(vector);
        Map<Tensor, Scalar> map = new HashMap<>();
        Scalar sum = RealScalar.ZERO;
        for (Entry<Tensor, Long> entry : navigableMap.entrySet()) {
            long add = entry.getValue();
            map.put(entry.getKey(), sum);
            sum = sum.add(RealScalar.of(add));
        }
        return Tensor.of(vector.stream().map(map::get));
    }

}
