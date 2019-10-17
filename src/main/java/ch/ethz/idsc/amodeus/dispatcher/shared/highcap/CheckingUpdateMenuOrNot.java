/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class CheckingUpdateMenuOrNot {
    private final Map<RoboTaxi, Set<AVRequest>> lastSetOfRequestsInRoute = new HashMap<>();
    private final Map<RoboTaxi, Set<AVRequest>> secondLastSetOfRequestInRoute = new HashMap<>();

    public boolean updateMenuOrNot(RoboTaxi roboTaxi, Set<AVRequest> setOfRequestsInRoute) {
        // prevent looping
        if (!secondLastSetOfRequestInRoute.containsKey(roboTaxi)) {
            secondLastSetOfRequestInRoute.put(roboTaxi, new HashSet<>());
        }
        if (!lastSetOfRequestsInRoute.containsKey(roboTaxi)) {
            lastSetOfRequestsInRoute.put(roboTaxi, new HashSet<>());
        }

        if (secondLastSetOfRequestInRoute.get(roboTaxi) == setOfRequestsInRoute) {
            secondLastSetOfRequestInRoute.put(roboTaxi, lastSetOfRequestsInRoute.get(roboTaxi));
            lastSetOfRequestsInRoute.put(roboTaxi, setOfRequestsInRoute);
            return false;
        }

        if (lastSetOfRequestsInRoute.get(roboTaxi) == setOfRequestsInRoute) {
            secondLastSetOfRequestInRoute.put(roboTaxi, lastSetOfRequestsInRoute.get(roboTaxi));
            lastSetOfRequestsInRoute.put(roboTaxi, setOfRequestsInRoute);
            return false; // if the requests in menu remain the same, then do not update menu
        }

        // Prevent update menu when dropping off/picking up
        if (!roboTaxi.getUnmodifiableViewOfCourses().isEmpty()) {
            if (roboTaxi.getDivertableLocation() == roboTaxi.getUnmodifiableViewOfCourses().get(0).getLink()) {
                return false; // if divertable location is equal to the link of first course, do not update menu
            }
        }

        secondLastSetOfRequestInRoute.put(roboTaxi, lastSetOfRequestsInRoute.get(roboTaxi));
        lastSetOfRequestsInRoute.put(roboTaxi, setOfRequestsInRoute);

        return true;
    }

}
