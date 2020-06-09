/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.Objects;

import org.matsim.amodeus.dvrp.schedule.AmodeusDriveTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.tracker.OnlineDriveTaskTracker;
import org.matsim.contrib.dvrp.util.LinkTimePair;

import amodeus.amodeus.util.math.GlobalAssert;

/** for vehicles that are currently driving, but should go to a new destination:
 * 1) change path of current drive task
 * 2) remove former stay task with old destination
 * 3) append new stay task */
/* package */ final class DriveVehicleDiversionDirective extends VehicleDiversionDirective {

    DriveVehicleDiversionDirective(RoboTaxi roboTaxi, Link destination, FuturePathContainer futurePathContainer) {
        super(roboTaxi, destination, futurePathContainer);
    }

    @Override
    void executeWithPath(VrpPathWithTravelData vrpPathWithTravelData) {
        final Schedule schedule = roboTaxi.getSchedule();
        final AmodeusDriveTask avDriveTask = (AmodeusDriveTask) schedule.getCurrentTask(); // <- implies that task is started
        final AmodeusStayTask avStayTask = (AmodeusStayTask) Schedules.getLastTask(schedule);
        final double scheduleEndTime = avStayTask.getEndTime();

        OnlineDriveTaskTracker taskTracker = (OnlineDriveTaskTracker) avDriveTask.getTaskTracker();
        LinkTimePair diversionPoint = Objects.requireNonNull(taskTracker.getDiversionPoint());
        
        boolean isCurrentLinkDiversion = diversionPoint.link == taskTracker.getPath().getLink(taskTracker.getCurrentLinkIdx());
        final int diversionLinkIndex = taskTracker.getCurrentLinkIdx() + (isCurrentLinkDiversion ? 0 : 1);
        
        final int lengthOfDiversion = vrpPathWithTravelData.getLinkCount();
        OnlineDriveTaskTracker onlineDriveTaskTracker = (OnlineDriveTaskTracker) taskTracker;
        final double newEndTime = vrpPathWithTravelData.getArrivalTime();

        if (newEndTime < scheduleEndTime)
            try {
                onlineDriveTaskTracker.divertPath(vrpPathWithTravelData);
                GlobalAssert.that(VrpPathUtils.isConsistent(avDriveTask.getPath()));

                final int lengthOfCombination = avDriveTask.getPath().getLinkCount();
                // System.out.println(String.format("[@%d of %d]", diversionLinkIndex, lengthOfCombination));
                if (diversionLinkIndex + lengthOfDiversion != lengthOfCombination)
                    throw new RuntimeException("mismatch " + diversionLinkIndex + "+" + lengthOfDiversion + " != " + lengthOfCombination);

                GlobalAssert.that(avDriveTask.getEndTime() == newEndTime);

                schedule.removeLastTask(); // remove former stay task with old destination
                ScheduleUtils.makeWhole(roboTaxi, newEndTime, scheduleEndTime, destination);
            } catch (Exception e) {
                System.err.println("Robotaxi ID: " + roboTaxi.getId().toString());
                System.err.println("====================================");
                System.err.println("Found problem with diversionLinkIdx!");
                System.err.println("====================================");
                throw new IllegalStateException();
            }
        else
            reportExecutionBypass(newEndTime - scheduleEndTime);
    }

}
