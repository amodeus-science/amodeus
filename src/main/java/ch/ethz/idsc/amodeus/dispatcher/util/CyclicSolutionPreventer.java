/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.UniversalDispatcher;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.amodeus.routing.EuclideanDistanceFunction;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/** If a {@link EuclideanDistanceFunction} is used for bipartite matching, then cycling of robotic taxis
 * may occur, i.e., an assignment is removed as the assigned taxi approaches in the network because its
 * network distance increases temporarily. The process repeats itself and leads to unrealistically long
 * waiting times. This class is used to remove such disimproving assignments. It will only take effect
 * if the {@link EuclideanDistanceFunction} was selected */
public enum CyclicSolutionPreventer {
    ;

    // TODO @clruch eliminate duplicate code, only difference is universalDispatcher,
    // shareduniversaldispatcher

    public static Map<RoboTaxi, PassengerRequest> apply(Map<RoboTaxi, PassengerRequest> assgnmt, UniversalDispatcher universalDispatcher, //
            DistanceFunction accDstFctn) {

        Map<RoboTaxi, PassengerRequest> assgnmtCopy = new HashMap<>(assgnmt);

        for (Entry<RoboTaxi, PassengerRequest> entry : assgnmtCopy.entrySet()) {
            /** previously assigned {@link RoboTaxi} */
            Optional<RoboTaxi> optional = universalDispatcher.getPickupTaxi(entry.getValue());
            if (optional.isPresent()) {
                final RoboTaxi prvTaxi = optional.get();
                final RoboTaxi newTaxi = entry.getKey();
                GlobalAssert.that(Objects.nonNull(newTaxi));
                if (!prvTaxi.equals(newTaxi)) {
                    double distNew = accDstFctn.getDistance(newTaxi, entry.getValue());
                    double distOld = accDstFctn.getDistance(prvTaxi, entry.getValue());
                    /** reassignment is prevented when the new taxi is not closer in terms of network distance */
                    if (distNew >= distOld) {
                        assgnmt.remove(newTaxi);
                        /** ensure that old assignment is still not divertable */
                        assgnmt.put(prvTaxi, entry.getValue());
                    }
                }
            }
        }
        return assgnmt;
    }

    public static Map<RoboTaxi, PassengerRequest> apply(Map<RoboTaxi, PassengerRequest> assgnmt, Function<PassengerRequest, RoboTaxi> getPickupTaxi, //
            DistanceFunction accDstFctn) {
        Map<RoboTaxi, PassengerRequest> assgnmtCopy = new HashMap<>(assgnmt);

        for (Entry<RoboTaxi, PassengerRequest> entry : assgnmtCopy.entrySet()) {
            /** previously assigned {@link RoboTaxi} */
            RoboTaxi prvTaxi = getPickupTaxi.apply(entry.getValue());
            if (Objects.nonNull(prvTaxi)) {
                final RoboTaxi newTaxi = entry.getKey();
                GlobalAssert.that(Objects.nonNull(newTaxi));
                if (!prvTaxi.equals(newTaxi)) {
                    double distNew = accDstFctn.getDistance(newTaxi, entry.getValue());
                    double distOld = accDstFctn.getDistance(prvTaxi, entry.getValue());
                    /** reassignment is prevented when the new taxi is not closer in terms of network distance */
                    if (distNew >= distOld) {
                        assgnmt.remove(newTaxi);
                        /** ensure that old assignment is still not divertable */
                        assgnmt.put(prvTaxi, entry.getValue());
                    }
                }
            }
        }
        return assgnmt;
    }
}
