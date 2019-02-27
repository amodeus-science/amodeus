package ch.ethz.idsc.amodeus.routing;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator;

public interface PathInterface<T> {

    public T fromTo(Link from, Link to, LeastCostPathCalculator calculator, Double now);

}
