/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.List;

import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/** for vehicles that are in stay task and should pickup a customer at the link:
 * 1) finish stay task 2) append pickup task 3) append drive task 4) append new stay task */
/* package */ final class SharedGeneralPickupDirective extends FuturePathDirective {
    final RoboTaxi roboTaxi;
    final List<AVRequest> sameOriginRequests;
    final double getTimeNow;

    public SharedGeneralPickupDirective(RoboTaxi roboTaxi, List<AVRequest> sameOriginRequests, //
            FuturePathContainer futurePathContainer, final double getTimeNow) {
        super(futurePathContainer);
        this.roboTaxi = roboTaxi;
        this.sameOriginRequests = sameOriginRequests;
        this.getTimeNow = getTimeNow;

        // all requests must have same from link
        GlobalAssert.that(sameOriginRequests.stream().map(AVRequest::getFromLink).distinct().count() == 1);
    }

    @Override
    void executeWithPath(final VrpPathWithTravelData vrpPathWithTravelData) {
        final Schedule schedule = roboTaxi.getSchedule();
        final AVStayTask avStayTask = (AVStayTask) Schedules.getLastTask(schedule);
        final double scheduleEndTime = avStayTask.getEndTime();
        GlobalAssert.that(scheduleEndTime == schedule.getEndTime());
        final double endTaskTime = vrpPathWithTravelData.getArrivalTime();

        if (endTaskTime < scheduleEndTime) {
            avStayTask.setEndTime(getTimeNow); // finish the last task now

            AVPickupTask pickupTask = new AVPickupTask( //
                    getTimeNow, // start of pickup
                    futurePathContainer.getStartTime(), // end of pickup
                    sameOriginRequests.get(0).getFromLink(), // location of driving start
                    0.0, sameOriginRequests);
            schedule.addTask(pickupTask);

            GlobalAssert.that(futurePathContainer.getStartTime() < scheduleEndTime);
            ScheduleUtils.makeWhole(roboTaxi, futurePathContainer.getStartTime(), scheduleEndTime, //
                    sameOriginRequests.get(0).getFromLink());

            // jan: following computation is mandatory for the internal scoring
            // // function
            final double distance = VrpPathUtils.getDistance(vrpPathWithTravelData);
            sameOriginRequests.forEach(r -> r.getRoute().setDistance(distance));// .getRoute().setDistance(distance);

        } else
            reportExecutionBypass(endTaskTime - scheduleEndTime);
    }

}
