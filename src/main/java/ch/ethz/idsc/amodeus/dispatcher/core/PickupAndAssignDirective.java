/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;

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

        // Update the roboTaxi menu // must be done for each request!
        // for (AVRequest request : commonOriginRequests)
        // roboTaxi.pickupNewCustomerOnBoard();
        roboTaxi.setCurrentDriveDestination(commonFromLink);

        
        System.out.println("before");
        roboTaxi.getUnmodifiableViewOfCourses().stream().forEach(c->{
            System.out.println(c);
        });
        
        
        List<SharedCourse> sharedCourses = roboTaxi.getUnmodifiableViewOfCourses();

        
        roboTaxi.pickupOf(commonOriginRequests);



        
        System.out.println("after");
        roboTaxi.getUnmodifiableViewOfCourses().stream().forEach(c->{
            System.out.println(c);
        });
        
        

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
