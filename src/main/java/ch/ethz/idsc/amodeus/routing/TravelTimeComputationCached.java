/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public class TravelTimeComputationCached extends CashedNetworkTimeDistance {

    public static TravelTimeComputationCached of(LeastCostPathCalculator calculator, //
            double maxLag) {
        return new TravelTimeComputationCached(calculator, maxLag);
    }

    private TravelTimeComputationCached(LeastCostPathCalculator calculator, Double maxLag) {
        super(calculator, maxLag);
    }

    @Override
    protected Scalar timePathComputation(Link from, Link to) {
        Path path = calculator.calcLeastCostPath(from.getFromNode(), to.getToNode(), now, null, null);
        return Quantity.of(path.travelTime, SI.SECOND);
    }
}
