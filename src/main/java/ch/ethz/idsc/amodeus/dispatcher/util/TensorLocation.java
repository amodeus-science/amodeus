/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.net.TensorCoords;
import ch.ethz.idsc.tensor.Tensor;
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

    private static Tensor ofLink(Link link) {
        return TensorCoords.toTensor(link.getCoord());
    }
}
