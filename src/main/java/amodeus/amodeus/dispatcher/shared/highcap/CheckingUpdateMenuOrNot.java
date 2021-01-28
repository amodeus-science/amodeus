/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.highcap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.schedule.directives.DefaultDriveDirective;
import amodeus.amodeus.dispatcher.core.schedule.directives.DefaultStopDirective;
import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;

/* package */ class CheckingUpdateMenuOrNot {
    private final Map<RoboTaxi, Set<PassengerRequest>> lastSetOfRequestsInRoute = new HashMap<>();
    private final Map<RoboTaxi, Set<PassengerRequest>> secondLastSetOfRequestInRoute = new HashMap<>();

    public boolean updateMenuOrNot(RoboTaxi roboTaxi, Set<PassengerRequest> setOfRequestsInRoute) {
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
            if (roboTaxi.getDivertableLocation() == Directive.getLink(roboTaxi.getUnmodifiableViewOfCourses().get(0)))
                return false; // if divertable location is equal to the link of first course, do not update menu

        secondLastSetOfRequestInRoute.put(roboTaxi, lastSetOfRequestsInRoute.get(roboTaxi));
        lastSetOfRequestsInRoute.put(roboTaxi, setOfRequestsInRoute);

        return true;
    }
}
