/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import org.matsim.contrib.drt.schedule.DrtDriveTask;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.tracker.OnlineDriveTaskTracker;
import org.matsim.contrib.dvrp.tracker.TaskTracker;

/** for vehicles that are currently driving, but should go to a new destination:
 * 1) change path of current drive task */
/* package */ final class DriveVehicleRerouteDirective extends FuturePathDirective {
    private final RoboTaxi roboTaxi;

    DriveVehicleRerouteDirective(FuturePathContainer futurePathContainer, RoboTaxi roboTaxi) {
        super(futurePathContainer);
        this.roboTaxi = roboTaxi;
    }

    @Override
    void executeWithPath(VrpPathWithTravelData vrpPathWithTravelData) {
        final Schedule schedule = roboTaxi.getSchedule();
        final DrtDriveTask avDriveTask = (DrtDriveTask) schedule.getCurrentTask(); // <- implies that task is started
        TaskTracker taskTracker = avDriveTask.getTaskTracker();
        OnlineDriveTaskTracker onlineDriveTaskTracker = (OnlineDriveTaskTracker) taskTracker;
        onlineDriveTaskTracker.divertPath(vrpPathWithTravelData);
    }
}
