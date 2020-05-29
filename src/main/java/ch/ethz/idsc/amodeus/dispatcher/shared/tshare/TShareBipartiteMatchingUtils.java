// TODO @clruch this must be elimitated as only minor difference to BipartiteMatchingUtils
/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedUniversalDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.GlobalBipartiteCost;
import ch.ethz.idsc.amodeus.dispatcher.util.GlobalBipartiteMatching;
import ch.ethz.idsc.amodeus.routing.CachedNetworkTimeDistance;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/* package */ class TShareBipartiteMatchingUtils {

    public Tensor executePickup(SharedUniversalDispatcher universalDispatcher, //
            Function<PassengerRequest, RoboTaxi> getPickupTaxi, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<PassengerRequest> requests, /** <- typically universalDispatcher.getPassengerRequests() */
            CachedNetworkTimeDistance distanceCashed, double now) {
        Tensor infoLine = Tensors.empty();

        Map<RoboTaxi, PassengerRequest> matching = ((new GlobalBipartiteMatching(new CachedDistanceCost(distanceCashed, now)))//
                .match(roboTaxis, requests));

        /** perform dispatching */
        matching.forEach(universalDispatcher::addSharedRoboTaxiPickup);
        return infoLine; // TODO @clruch always empty?
    }

    /** Use the Cacheddistance calculator from the TShare dispatcher to compute the
     * cost for the bipartite matching. */
    private class CachedDistanceCost implements GlobalBipartiteCost {
        private CachedNetworkTimeDistance distanceCashed;
        private double now;

        public CachedDistanceCost(CachedNetworkTimeDistance distanceCashed, double now) {
            this.distanceCashed = distanceCashed;
            this.now = now;
        }

        @Override
        public double between(RoboTaxi roboTaxi, Link link) {
            return distanceCashed.distance(roboTaxi.getDivertableLocation(), link, now).number().doubleValue();

        }
    }

}
