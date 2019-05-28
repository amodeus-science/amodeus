package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.UniversalDispatcher;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.matsim.av.passenger.AVRequest;

public interface BipartiteMatcher {

    public Tensor executePickup(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            DistanceFunction distanceFunction, Network network);

}
