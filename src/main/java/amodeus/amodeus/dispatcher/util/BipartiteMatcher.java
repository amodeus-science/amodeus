/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.util;

import java.util.Collection;

import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.SharedUniversalDispatcher;
import amodeus.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.tensor.Tensor;

@FunctionalInterface
public interface BipartiteMatcher {
    Tensor executePickup(SharedUniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<PassengerRequest> requests, /** <- typically universalDispatcher.getPassengerRequests() */
            DistanceFunction distanceFunction, Network network);
}
