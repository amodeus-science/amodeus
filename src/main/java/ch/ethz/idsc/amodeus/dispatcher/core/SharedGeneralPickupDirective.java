/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Arrays;

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
    final AVRequest currentRequest;
    final double getTimeNow;

    public SharedGeneralPickupDirective(RoboTaxi roboTaxi, AVRequest currentRequest, //
            FuturePathContainer futurePathContainer, final double getTimeNow) {
        super(futurePathContainer);
        this.roboTaxi = roboTaxi;
        this.currentRequest = currentRequest;
        this.getTimeNow = getTimeNow;
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
                    currentRequest.getFromLink(), // location of driving start
                    0.0, //
                    Arrays.asList(currentRequest));
            // pickupTask.addRequest(currentRequest); // serving only one request at a time
            schedule.addTask(pickupTask);

            // schedule.addTask(new AVDriveTask( //
            // vrpPathWithTravelData, Arrays.asList(currentRequest)));
            // ScheduleUtils.makeWhole(robotaxi, endTaskTime, scheduleEndTime, vrpPathWithTravelData.getToLink());

            GlobalAssert.that(futurePathContainer.getStartTime() < scheduleEndTime);
            ScheduleUtils.makeWhole(roboTaxi, futurePathContainer.getStartTime(), scheduleEndTime, currentRequest.getFromLink());

            // jan: following computation is mandatory for the internal scoring
            // // function
            final double distance = VrpPathUtils.getDistance(vrpPathWithTravelData);
            currentRequest.getRoute().setDistance(distance);
        } else
            reportExecutionBypass(endTaskTime - scheduleEndTime);
    }

}
