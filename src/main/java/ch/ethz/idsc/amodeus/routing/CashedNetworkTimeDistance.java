package ch.ethz.idsc.amodeus.routing;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

public class CashedNetworkTimeDistance implements NetworkTimeDistInterface {

    private final CashedNetworkPropertyComputation<Tensor> cachedPathComputation;

    /** A {@link CashedNetworkTimeDistance} stores all the calculated travel times
     * which were calculated no longer ago than @param maxLag. The underlying logic is that in this manner
     * the expensive routing computation has to be done fewer times for identical pairs
     * of {@link Link}s.For the routing, different {@link LeastCostPathCalculator}s can be used,
     * e.g., to minimize traveltime or network distance. If the boolean @param cachePath is set to
     * true, then the computed Pathes are stored as well (memory intensive!) */
    public CashedNetworkTimeDistance(LeastCostPathCalculator calculator, Double maxLag, NetworkPropertyInterface<Tensor> pathInterface) {
        this.cachedPathComputation = new CashedNetworkPropertyComputation<>(calculator, maxLag, pathInterface);
    }

    // /** removes computations that happened more time than @param maxLag ago since @param now */
    // public final void update(Double now) {
    // cachedPathComputation.update(now);
    // }

    public boolean checkTime(double now) {
        return cachedPathComputation.checkTime(now);
    }

    @Override
    public Scalar travelTime(Link from, Link to, Double now) {
        Tensor timeDist = cachedPathComputation.fromTo(from, to, now);
        return timeDist.Get(0);
    }

    @Override
    public Scalar distance(Link from, Link to, Double now) {
        Tensor timeDist = cachedPathComputation.fromTo(from, to, now);
        return timeDist.Get(1);
    }
}
