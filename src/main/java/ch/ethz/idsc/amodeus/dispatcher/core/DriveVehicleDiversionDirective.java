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
 * 1) change path of current drive task
 * 2) remove former stay task with old destination
 * 3) append new stay task */
/* package */ final class DriveVehicleDiversionDirective extends VehicleDiversionDirective {

    DriveVehicleDiversionDirective(UnitCapRoboTaxi robotaxi, Link destination, FuturePathContainer futurePathContainer) {
        super(robotaxi, destination, futurePathContainer);
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
                onlineDriveTaskTracker.divertPath(vrpPathWithTravelData);
                GlobalAssert.that(VrpPathUtils.isConsistent(avDriveTask.getPath()));

                final int lengthOfCombination = avDriveTask.getPath().getLinkCount();
                // System.out.println(String.format("[@%d of %d]", diversionLinkIndex, lengthOfCombination));
                if (diversionLinkIndex + lengthOfDiversion != lengthOfCombination)
                    throw new RuntimeException("mismatch " + diversionLinkIndex + "+" + lengthOfDiversion + " != " + lengthOfCombination);

                GlobalAssert.that(avDriveTask.getEndTime() == newEndTime);

                schedule.removeLastTask(); // remove former stay task with old destination
                ScheduleUtils.makeWhole(robotaxi, newEndTime, scheduleEndTime, destination);
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
