/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

/* package */ enum PathProperty implements NetworkPropertyInterface<Path> {
    INSTANCE;

    @Override // from NetworkPropertyInterface
    public Path fromTo(Link from, Link to, LeastCostPathCalculator calculator, Double now) {
        /** path */
        return StaticHelper.pathBetween(from, to, calculator, now);
    }
}
