/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

public enum DrivebyRequestStopper {
    ;

    /** Matches all {@link RoboTaxi} @param roboTaxis to the requests sorted in @param requestLocs
     * if they are located on the same {@link Link} with the @param biConsumer, every request is only
     * matched once.
     * 
     * @return {@link Map} with the matchings */
    public static Map<RoboTaxi, AVRequest> stopDrivingBy(Map<Link, List<AVRequest>> requestLocs, Collection<RoboTaxi> roboTaxis, //
            BiConsumer<RoboTaxi, AVRequest> biConsumer) {
        Map<RoboTaxi, AVRequest> pickups = new HashMap<>();
        for (RoboTaxi roboTaxi : roboTaxis) {
            Link link = roboTaxi.getDivertableLocation();
            if (requestLocs.containsKey(link)) {
                List<AVRequest> requestList = requestLocs.get(link);
                if (!requestList.isEmpty()) {
                    AVRequest request = requestList.get(0);
                    biConsumer.accept(roboTaxi, request);
                    pickups.put(roboTaxi, request);
                    requestList.remove(0);
                }
            }
        }
        GlobalAssert.that(pickups.values().stream().distinct().count() == pickups.size());
        return pickups;
    }
}