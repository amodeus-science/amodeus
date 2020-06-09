/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.util;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.net.TensorCoords;
import ch.ethz.idsc.tensor.Tensor;

public enum TensorLocation {
    ;

    public static Tensor of(PassengerRequest avRequest) {
        return ofLink(avRequest.getFromLink());
    }

    public static Tensor of(RoboTaxi roboTaxi) {
        return ofLink(roboTaxi.getDivertableLocation());
    }

    public static Tensor of(Link link) {
        return ofLink(link);
    }

    private static Tensor ofLink(Link link) {
        return TensorCoords.toTensor(link.getCoord());
    }
}
