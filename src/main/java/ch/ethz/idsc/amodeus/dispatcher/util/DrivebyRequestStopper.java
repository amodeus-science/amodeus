/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.UnitCapRoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

/** * call with setVehiclePickup as biConsumer, this class stops vehicles
 * which are diving by an open request. */
public enum DrivebyRequestStopper {
    ;

    public static int stopDrivingBy(Map<Link, List<AVRequest>> requestLocs, Collection<UnitCapRoboTaxi> roboTaxis, BiConsumer<UnitCapRoboTaxi, AVRequest> biConsumer) {
        int numDriveByPickup = 0;

        for (UnitCapRoboTaxi roboTaxi : roboTaxis) {
            Link link = roboTaxi.getDivertableLocation();
            if (requestLocs.containsKey(link)) {
                List<AVRequest> requestList = requestLocs.get(link);
                if (!requestList.isEmpty()) {
                    AVRequest request = requestList.get(0);
                    biConsumer.accept(roboTaxi, request);
                    ++numDriveByPickup;
                }
            }
        }
        return numDriveByPickup;
    }
}
