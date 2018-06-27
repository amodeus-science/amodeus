/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.tracker.OnlineDriveTaskTracker;
import org.matsim.contrib.dvrp.tracker.TaskTracker;

import ch.ethz.idsc.amodeus.matsim.mod.AmodeusDriveTaskTracker;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/** for vehicles that are currently driving, but should go to a new destination:
 * 1) change path of current drive task 2) remove former stay task with old
 * destination 3) append new stay task */
/* package */ final class SharedGeneralDriveDiversionDirective extends VehicleDiversionDirective {
    final SharedRoboTaxi robotaxi;
    final double getTimeNow;

    SharedGeneralDriveDiversionDirective(SharedRoboTaxi robotaxi, Link destLink, //
            FuturePathContainer futurePathContainer, final double getTimeNow) {
        super(robotaxi, destLink, futurePathContainer);
        this.robotaxi = robotaxi;
        this.getTimeNow = getTimeNow;
    }

    @Override
    void executeWithPath(VrpPathWithTravelData vrpPathWithTravelData) {
        final Schedule schedule = robotaxi.getSchedule();
        final AVDriveTask avDriveTask = (AVDriveTask) schedule.getCurrentTask(); // <- implies that task is started
        final AVStayTask avStayTask = (AVStayTask) Schedules.getLastTask(schedule);
        final double scheduleEndTime = avStayTask.getEndTime();

        TaskTracker taskTracker = avDriveTask.getTaskTracker();
        AmodeusDriveTaskTracker onlineDriveTaskTrackerImpl = (AmodeusDriveTaskTracker) taskTracker;
        final int diversionLinkIndex = onlineDriveTaskTrackerImpl.getDiversionLinkIndex();
        final int lengthOfDiversion = vrpPathWithTravelData.getLinkCount();
        OnlineDriveTaskTracker onlineDriveTaskTracker = (OnlineDriveTaskTracker) taskTracker;
        final double newEndTime = vrpPathWithTravelData.getArrivalTime();

        if (newEndTime < scheduleEndTime) {

            try {
                GlobalAssert.that(VrpPathUtils.isConsistent(avDriveTask.getPath()));

                final int lengthOfCombination = avDriveTask.getPath().getLinkCount();
                // System.out.println(String.format("[@%d of %d]", diversionLinkIndex,
                // lengthOfCombination));
                if (diversionLinkIndex + lengthOfDiversion != lengthOfCombination)
                    throw new RuntimeException("mismatch " + diversionLinkIndex + "+" + lengthOfDiversion + " != " + lengthOfCombination);

                // FIXME
                GlobalAssert.that(avDriveTask.getEndTime() == newEndTime);

                // Remove all pending tasks in the future
                schedule.removeLastTask();
                GlobalAssert.that(Schedules.getLastTask(schedule) == schedule.getCurrentTask());

                ScheduleUtils.makeWhole(robotaxi, newEndTime, scheduleEndTime, destination);

                // jan: following computation is mandatory for the internal scoring
                // function
                // final double distance = VrpPathUtils.getDistance(vrpPathWithTravelData);
                // nextRequest.getRoute().setDistance(distance);

            } catch (Exception e) {
                System.err.println("Robotaxi ID: " + robotaxi.getId().toString());
                System.err.println("====================================");
                System.err.println("Found problem with diversionLinkIdx!");
                System.err.println("====================================");
            }

        } else
            reportExecutionBypass(newEndTime - scheduleEndTime);
    }

}
