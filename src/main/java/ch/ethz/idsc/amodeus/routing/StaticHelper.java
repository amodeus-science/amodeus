/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.router.util.LeastCostPathCalculator;

/* package */ class StaticHelper {
    public static LeastCostPathCalculator.Path pathBetween(Link from, Link to, LeastCostPathCalculator calculator, Double now) {
        return pathBetween(Objects.requireNonNull(from).getFromNode(), Objects.requireNonNull(to).getToNode(), calculator, now);
    }

    public static LeastCostPathCalculator.Path pathBetween(Node from, Node to, LeastCostPathCalculator calculator, Double now) {
        return calculator.calcLeastCostPath(Objects.requireNonNull(from), Objects.requireNonNull(to), now, null, null);
    }

    public static double length(LeastCostPathCalculator.Path path) {
        return path.links.stream().mapToDouble(Link::getLength).sum();
    }
}
