/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedUniversalDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.core.UniversalDispatcher;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

/** If a {@link EuclideanDistanceFunction} is used for bipartite matching, then cycling of robotic taxis
 * may occur, i.e., an assignment is removed as the assigned taxi approaches in the network because its
 * network distance increases temporarily. The process repeats itself and leads to unrealistically long
 * waiting times. This class is used to remove such disimproving assignments. It will only take effect
 * if the {@link EuclideanDistanceFunction} was selected */
/* package */ enum CyclicSolutionPreventer {
    ;

    // TODO eliminate duplicate code, only difference is universalDispatcher,
    // shareduniversaldispatcher

    public static Map<RoboTaxi, AVRequest> apply(Map<RoboTaxi, AVRequest> assgnmt, UniversalDispatcher universalDispatcher, //
            DistanceFunction accDstFctn) {

        Map<RoboTaxi, AVRequest> assgnmtCopy = new HashMap<>(assgnmt);

        for (Entry<RoboTaxi, AVRequest> entry : assgnmtCopy.entrySet()) {
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

    public static Map<RoboTaxi, AVRequest> apply(Map<RoboTaxi, AVRequest> assgnmt, Function<AVRequest, RoboTaxi> getPickupTaxi, //
            DistanceFunction accDstFctn) {

        Map<RoboTaxi, AVRequest> assgnmtCopy = new HashMap<>(assgnmt);

        for (Entry<RoboTaxi, AVRequest> entry : assgnmtCopy.entrySet()) {
            /** previously assigned {@link RoboTaxi} */

            RoboTaxi roboTaxi = getPickupTaxi.apply(entry.getValue());
            if (Objects.nonNull(roboTaxi)) {
                final RoboTaxi prvTaxi = roboTaxi;
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
