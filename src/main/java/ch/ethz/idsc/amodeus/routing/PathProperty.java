/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

/* package */ enum PathProperty implements NetworkPropertyInterface<Path> {
    INSTANCE;

    @Override // from NetworkPropertyInterface
    public Path fromTo(Link from, Link to, LeastCostPathCalculator calculator, Double now) {
        return fromTo(Objects.requireNonNull(from).getFromNode(), Objects.requireNonNull(to).getToNode(), calculator, now);
    }

    public Path fromTo(Node from, Node to, LeastCostPathCalculator calculator, Double now) {
        return calculator.calcLeastCostPath(Objects.requireNonNull(from), Objects.requireNonNull(to), now, null, null);
    }

    public static double length(LeastCostPathCalculator.Path path) {
        return path.links.stream().mapToDouble(Link::getLength).sum();
    }
}
