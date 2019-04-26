/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.tracker.OnlineDriveTaskTracker;
import org.matsim.contrib.dvrp.tracker.TaskTracker;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.schedule.AVDriveTask;

/** for vehicles that are currently driving, but should go to a new destination:
 * 1) change path of current drive task
 * /* package */
final class DriveVehicleRerouteDirective extends FuturePathDirective {
    // field
    RoboTaxi roboTaxi;

    // constructor
    DriveVehicleRerouteDirective(FuturePathContainer futurePathContainer, RoboTaxi roboTaxi) {
        super(futurePathContainer);
        this.roboTaxi = roboTaxi;
    }

    // methods
    @Override
    void executeWithPath(VrpPathWithTravelData vrpPathWithTravelData) {
        final Schedule schedule = roboTaxi.getSchedule();
        final AVDriveTask avDriveTask = (AVDriveTask) schedule.getCurrentTask(); // <- implies that task is started
        TaskTracker taskTracker = avDriveTask.getTaskTracker();
        OnlineDriveTaskTracker onlineDriveTaskTracker = (OnlineDriveTaskTracker) taskTracker;
        try {
            onlineDriveTaskTracker.divertPath(vrpPathWithTravelData);
            GlobalAssert.that(VrpPathUtils.isConsistent(avDriveTask.getPath()));
        } catch (Exception e) {
            System.err.println("Robotaxi ID: " + roboTaxi.getId().toString());
            System.err.println("====================================");
            System.err.println("Something Wrong with Rerouting!!!!!!");
            System.err.println("====================================");
        }
    }

}
