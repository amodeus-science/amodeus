/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.routing;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;

public enum TimeDistanceProperty implements NetworkPropertyInterface<Tensor> {
    INSTANCE;

    @Override
    public Tensor fromTo(Link from, Link to, LeastCostPathCalculator calculator, Double now) {
        /** path */
        Path path = calculator.calcLeastCostPath(from.getFromNode(), to.getToNode(), now, null, null);
        /** travel time */
        Scalar travelTime = Quantity.of(path.travelTime, SI.SECOND);
        /** path length */
        double dist = 0.0;
        for (Link link : path.links)
            dist += link.getLength();
        Scalar distance = Quantity.of(dist, SI.METER);
        /** return pair */
        return Tensors.of(travelTime, distance);
    }

}
