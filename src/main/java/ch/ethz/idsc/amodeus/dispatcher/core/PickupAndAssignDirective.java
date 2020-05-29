/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.List;

import org.matsim.amodeus.dvrp.request.AVRequest;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ class PickupAndAssignDirective {

    public static void using(RoboTaxi roboTaxi, List<AVRequest> commonOriginRequests, //
            double now, double pickupDurationPerStop, FuturePathFactory futurePathFactory) {

        // all requests must have same from link
        GlobalAssert.that(commonOriginRequests.stream().map(AVRequest::getFromLink).distinct().count() == 1);
        Link commonFromLink = commonOriginRequests.get(0).getFromLink();

        // ensure that roboTaxi has enough capacity
        int onBoard = (int) roboTaxi.getOnBoardPassengers();
        int pickupN = commonOriginRequests.size();
        GlobalAssert.that(onBoard + pickupN <= roboTaxi.getCapacity());

        // set drive destination to pickup link
        roboTaxi.setCurrentDriveDestination(commonFromLink);

        // ensure that the pickup tasks are removed from the menu
        roboTaxi.pickupOf(commonOriginRequests);

        // Assign Directive
        final double endPickupTime = now + pickupDurationPerStop;
        FuturePathContainer futurePathContainer = //
                futurePathFactory.createFuturePathContainer(commonFromLink, //
                        SharedRoboTaxiUtils.getStarterLink(roboTaxi), endPickupTime);
        roboTaxi.assignDirective(new SharedGeneralPickupDirective(roboTaxi, commonOriginRequests, //
                futurePathContainer, now));

        GlobalAssert.that(!roboTaxi.isDivertable());

        // ensure that pickup is not in taxi schedule, drop-off still is
        for (AVRequest avRequest2 : commonOriginRequests) {
            GlobalAssert.that(!roboTaxi.getUnmodifiableViewOfCourses().contains(SharedCourse.pickupCourse(avRequest2)));
            GlobalAssert.that(roboTaxi.getUnmodifiableViewOfCourses().contains(SharedCourse.dropoffCourse(avRequest2)));
        }

    }

}
