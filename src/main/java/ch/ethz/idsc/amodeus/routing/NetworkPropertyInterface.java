package ch.ethz.idsc.amodeus.routing;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator;

/** A {@link NetworkPropertyInterface} is used in the {@link CachedNetworkPropertyComputation}
 * to compute a property @param <T> of the network defined on a pair of {@link Link}s and a
 * time. The computation is done via a {@link LeastCostPathCalculator}. */
public interface NetworkPropertyInterface<T> {

    /** @return the property T of the network defined between the {@link Link}s @param from
     *         and @param to at time @param now using the {@link LeastCostPathCalculator} @param calculator.
     *         The resulting property T may then be cached to speed up computation times. */
    public T fromTo(Link from, Link to, LeastCostPathCalculator calculator, Double now);

}
