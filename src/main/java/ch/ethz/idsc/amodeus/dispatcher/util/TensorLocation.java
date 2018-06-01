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
        return ofLink(avRequest.getFromLink());
    }

    public static Tensor of(RoboTaxi robotaxi) {
        return ofLink(robotaxi.getDivertableLocation());
    }

    public static Tensor of(Link link) {
        return ofLink(link);
    }

    public static Tensor of(Coord coord) {
        return Tensors.vectorDouble(coord.getX(), coord.getY());
    }

    private static Tensor ofLink(Link link) {
        return of(link.getCoord());
    }
}
