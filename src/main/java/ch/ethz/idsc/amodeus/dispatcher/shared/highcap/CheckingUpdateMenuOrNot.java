/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.amodeus.dvrp.request.AVRequest;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/* package */ class CheckingUpdateMenuOrNot {
    private final Map<RoboTaxi, Set<AVRequest>> lastSetOfRequestsInRoute = new HashMap<>();
    private final Map<RoboTaxi, Set<AVRequest>> secondLastSetOfRequestInRoute = new HashMap<>();

    public boolean updateMenuOrNot(RoboTaxi roboTaxi, Set<AVRequest> setOfRequestsInRoute) {
        // prevent looping
        secondLastSetOfRequestInRoute.computeIfAbsent(roboTaxi, rt -> new HashSet<>());

        lastSetOfRequestsInRoute.computeIfAbsent(roboTaxi, rt -> new HashSet<>());

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
        if (!roboTaxi.getUnmodifiableViewOfCourses().isEmpty())
            if (roboTaxi.getDivertableLocation() == roboTaxi.getUnmodifiableViewOfCourses().get(0).getLink())
                return false; // if divertable location is equal to the link of first course, do not update menu

        secondLastSetOfRequestInRoute.put(roboTaxi, lastSetOfRequestsInRoute.get(roboTaxi));
        lastSetOfRequestsInRoute.put(roboTaxi, setOfRequestsInRoute);

        return true;
    }
}
