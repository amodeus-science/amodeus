/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.DriveTask;
import org.matsim.contrib.dvrp.schedule.Schedule;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/** for vehicles that are currently driving, but should go to a new destination:
 * 1) change path of current drive task 2) remove former stay task with old
 * destination 3) append new stay task */
/** @author Nicolo Ormezzano, Lukas Sieber */
/* package */ final class SharedGeneralStayDirective extends VehicleDiversionDirective {
    final RoboTaxi robotaxi;
    final double getTimeNow;

    SharedGeneralStayDirective(RoboTaxi robotaxi, Link destLink, //
            FuturePathContainer futurePathContainer, final double getTimeNow) {
        super(robotaxi, destLink, futurePathContainer);
        this.robotaxi = robotaxi;
        this.getTimeNow = getTimeNow;
    }

    @Override
    void executeWithPath(VrpPathWithTravelData vrpPathWithTravelData) {
        final Schedule schedule = robotaxi.getSchedule();
        final AVStayTask avStayTask = (AVStayTask) schedule.getCurrentTask(); // <- implies that task is started
        final double scheduleEndTime = avStayTask.getEndTime(); // typically 108000.0
        GlobalAssert.that(scheduleEndTime == schedule.getEndTime());

        final double endDriveTask = vrpPathWithTravelData.getArrivalTime();

        if (endDriveTask < scheduleEndTime) {

            GlobalAssert.that(vrpPathWithTravelData.getDepartureTime() == robotaxi.getDivertableTime());
            avStayTask.setEndTime(vrpPathWithTravelData.getDepartureTime());

            DriveTask driveTask = new AVDriveTask( //
                    vrpPathWithTravelData);
            schedule.addTask(driveTask);

            ScheduleUtils.makeWhole(robotaxi, endDriveTask, scheduleEndTime, destination);

            // jan: following computation is mandatory for the internal scoring
            // function
            // final double distance = VrpPathUtils.getDistance(vrpPathWithTravelData);
            // nextRequest.getRoute().setDistance(distance);

        } else
            reportExecutionBypass(endDriveTask - scheduleEndTime);
    }

}
