/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.List;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;

import amodeus.amodeus.dispatcher.shared.SharedCourse;
import amodeus.amodeus.util.math.GlobalAssert;

/* package */ class PickupAndAssignDirective {

    public static void using(RoboTaxi roboTaxi, List<PassengerRequest> commonOriginRequests, //
            double now, double pickupDurationPerStop, FuturePathFactory futurePathFactory) {

        // all requests must have same from link
        GlobalAssert.that(commonOriginRequests.stream().map(PassengerRequest::getFromLink).distinct().count() == 1);
        Link commonFromLink = commonOriginRequests.get(0).getFromLink();

        // ensure that roboTaxi has enough capacity
        int onBoard = (int) roboTaxi.getOnBoardPassengers();
        int pickupN = commonOriginRequests.size();
        GlobalAssert.that(onBoard + pickupN <= roboTaxi.getCapacity());

        // set drive destination to pickup link
        roboTaxi.setCurrentDriveDestination(commonFromLink);

        // ensure that the pickup tasks are removed from the menu
        // roboTaxi.pickupOf(commonOriginRequests);

        // Assign Directive
        roboTaxi.assignDirective(new SharedGeneralPickupDirective(roboTaxi, commonOriginRequests, //
                pickupDurationPerStop, now));

        GlobalAssert.that(!roboTaxi.isDivertable());

        for (PassengerRequest avRequest2 : commonOriginRequests) {
            GlobalAssert.that(roboTaxi.getUnmodifiableViewOfCourses().contains(SharedCourse.pickupCourse(avRequest2)));
            GlobalAssert.that(roboTaxi.getUnmodifiableViewOfCourses().contains(SharedCourse.dropoffCourse(avRequest2)));
        }

    }

}
