package ch.ethz.idsc.amodeus.routing;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

public enum PathProperty implements NetworkPropertyInterface<Path> {
    INSTANCE;

    @Override
    public Path fromTo(Link from, Link to, LeastCostPathCalculator calculator, Double now) {
        /** path */
        Path path = calculator.calcLeastCostPath(from.getFromNode(), to.getToNode(), now, null, null);
        return path;
    }

}
