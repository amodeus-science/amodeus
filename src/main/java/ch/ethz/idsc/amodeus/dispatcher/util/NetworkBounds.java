/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public enum NetworkBounds {
    ;

    /** function does not give tight bounds but uses the max of
     * width and height in both directions
     * 
     * @param network
     * @return {{lower}, {upper}} */
    public static Tensor of(Network network) {
        double[] bounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        return Tensors.of( //
                Tensors.vectorDouble(bounds[0], bounds[1]), //
                upper(bounds));
    }

    public static Tensor lowerBoundsOf(Network network) {
        double[] bounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        return Tensors.vectorDouble(bounds[0], bounds[1]);
    }

    private static Tensor upper(double[] bounds) {
        Tensor lbounds = Tensors.vectorDouble(bounds[0], bounds[1]);
        Tensor ubounds = Tensors.vectorDouble(bounds[2], bounds[3]);
        Tensor diff = ubounds.subtract(lbounds);
        double dx = diff.Get(0).number().doubleValue();
        double dy = diff.Get(1).number().doubleValue();
        GlobalAssert.that(dx > 0);
        GlobalAssert.that(dy > 0);
        double dmax = Math.max(dx, dy);
        return lbounds.add(Tensors.vectorDouble(dmax, dmax));
    }

    public static Tensor upperBoundsOf(Network network) {
        double[] bounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        Tensor lbounds = Tensors.vectorDouble(bounds[0], bounds[1]);
        Tensor ubounds = Tensors.vectorDouble(bounds[2], bounds[3]);
        Tensor diff = ubounds.subtract(lbounds);
        double dx = diff.Get(0).number().doubleValue();
        double dy = diff.Get(1).number().doubleValue();
        GlobalAssert.that(dx > 0);
        GlobalAssert.that(dy > 0);
        double dmax = Math.max(dx, dy);
        return (lbounds.add(Tensors.vectorDouble(dmax, dmax)));
    }

}
