/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/** for vehicles that are in stay task and should pickup a customer at the link:
 * 1) finish stay task 2) append pickup task 3) append drive task 4) append
 * dropoff task 5) append new stay task */
/** @author Nicolo Ormezzano, Lukas Sieber */
/* package */ final class SharedGeneralPickupOrDropoffDiversionDirective extends FuturePathDirective {
    final RoboTaxi robotaxi;
    final double getTimeNow;

    public SharedGeneralPickupOrDropoffDiversionDirective(RoboTaxi robotaxi, //
            FuturePathContainer futurePathContainer, final double getTimeNow) {
        super(futurePathContainer);
        this.robotaxi = robotaxi;
        this.getTimeNow = getTimeNow;
    }

    @Override
    void executeWithPath(final VrpPathWithTravelData vrpPathWithTravelData) {
        final Schedule schedule = robotaxi.getSchedule();
        final Task currentTask = schedule.getCurrentTask();
        final AVStayTask avStayTask = (AVStayTask) Schedules.getLastTask(schedule);
        final double scheduleEndTime = avStayTask.getEndTime();
        final double endTaskTime = currentTask.getEndTime();
        GlobalAssert.that(scheduleEndTime == schedule.getEndTime());

        if (endTaskTime < scheduleEndTime) {

            // Remove all pending tasks in the future
            while (Schedules.getLastTask(schedule).getEndTime() != schedule.getCurrentTask().getEndTime()) {
                schedule.removeLastTask();
            }

            // Add new drive task
            schedule.addTask(new AVDriveTask( //
                    vrpPathWithTravelData));

            ScheduleUtils.makeWhole(robotaxi, endTaskTime, scheduleEndTime, vrpPathWithTravelData.getToLink());

            // jan: following computation is mandatory for the internal scoring
            // function
            // final double distance = VrpPathUtils.getDistance(vrpPathWithTravelData);
            // nextRequest.getRoute().setDistance(distance);

        } else
            reportExecutionBypass(endTaskTime - scheduleEndTime);
    }

}
