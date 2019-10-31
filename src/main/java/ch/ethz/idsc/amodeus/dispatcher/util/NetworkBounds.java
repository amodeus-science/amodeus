/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Max;
import ch.ethz.idsc.tensor.sca.Sign;

public enum NetworkBounds {
    ;

    /** function does not give tight bounds but uses the max of
     * width and height in both directions
     * 
     * @param network
     * @return {{lower}, {square}} */
    public static Tensor of(Network network) {
        double[] bounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        return Tensors.of(lowerBoundsOf(bounds), square(bounds));
    }

    public static Tensor lowerBoundsOf(Network network) {
        return lowerBoundsOf(NetworkUtils.getBoundingBox(network.getNodes().values()));
    }

    private static Tensor lowerBoundsOf(double[] bounds) {
        return Tensors.vectorDouble(bounds[0], bounds[1]);
    }

    public static Tensor upperBoundsOf(Network network) {
        return lowerBoundsOf(NetworkUtils.getBoundingBox(network.getNodes().values()));
    }

    private static Tensor upperBoundsOf(double[] bounds) {
        return Tensors.vectorDouble(bounds[2], bounds[3]);
    }

    public static Tensor square(Network network) {
        return square(NetworkUtils.getBoundingBox(network.getNodes().values()));
    }

    private static Tensor square(double[] bounds) {
        Tensor lbounds = lowerBoundsOf(bounds);
        Tensor ubounds = upperBoundsOf(bounds);
        // Tensor diff = ubounds.subtract(lbounds);
        // double dx = diff.Get(0).number().doubleValue();
        // double dy = diff.Get(1).number().doubleValue();
        // GlobalAssert.that(dx > 0);
        // GlobalAssert.that(dy > 0);
        // double dmax = Math.max(dx, dy);
        // return lbounds.add(Tensors.vectorDouble(dmax, dmax));
        Tensor diff = ubounds.subtract(lbounds).map(Sign::requirePositive);
        Scalar dmax = Max.of(diff.Get(0), diff.Get(1));
        return lbounds.map(dmax::add);
    }
}
