/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.tracker.OnlineDriveTaskTracker;
import org.matsim.contrib.dvrp.tracker.TaskTracker;
import org.matsim.contrib.dvrp.util.LinkTimePair;

import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/* package */ class RoboTaxiLocation extends RoboTaxiTaskAdapter {

    /** @param avVehicle
     * @return link or null with a small chance */
    public static Link of(RoboTaxi robotaxi) {
        Schedule schedule = Objects.requireNonNull(robotaxi.getSchedule());
        /** {@link ScheduleImpl.failIfNotStarted} triggers, very likely you have
         * entered a simulation start time other than 0:00. Check that in the
         * av_config.xml file. */
        return new RoboTaxiLocation(schedule.getCurrentTask()).link;
    }

    /** DO NOT INITIALIZE THE LINK VARIABLE !!! */
    private Link link;

    private RoboTaxiLocation(Task task) {
        super(task);
    }

    @Override
    public void handle(AVPickupTask avPickupTask) {
        link = avPickupTask.getLink();
    }

    @Override
    public void handle(AVDropoffTask avDropoffTask) {
        link = avDropoffTask.getLink();
    }

    @Override
    public void handle(AVDriveTask avDriveTask) {
        TaskTracker taskTracker = avDriveTask.getTaskTracker();
        OnlineDriveTaskTracker onlineDriveTaskTracker = (OnlineDriveTaskTracker) taskTracker;
        // there is a slim chance that function getDiversionPoint() returns null
        LinkTimePair linkTimePair = onlineDriveTaskTracker.getDiversionPoint();
        if (linkTimePair != null)
            link = linkTimePair.link;
    }

    @Override
    public void handle(AVStayTask avStayTask) {
        link = avStayTask.getLink();
    }
}
