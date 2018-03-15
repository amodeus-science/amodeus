/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.passenger.AVRequest;

public enum TensorLocation {
    ;

    public static Tensor of(AVRequest avRequest) {
        return tensorOf(avRequest.getFromLink());
    }

    public static Tensor of(RoboTaxi robotaxi) {
        return tensorOf(robotaxi.getDivertableLocation());
    }

    public static Tensor of(Link link) {
        return tensorOf(link);
    }

    private static Tensor tensorOf(Link link) {
        Coord coord = link.getCoord();
        return Tensors.vectorDouble(coord.getX(), coord.getY());
    }
}
