/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.Objects;

import org.matsim.amodeus.dvrp.schedule.AmodeusDriveTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusDropoffTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusPickupTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.tracker.OnlineDriveTaskTracker;
import org.matsim.contrib.dvrp.tracker.TaskTracker;
import org.matsim.contrib.dvrp.util.LinkTimePair;

/* package */ class RoboTaxiLocation extends RoboTaxiTaskAdapter {

    /** @param roboTaxi
     * @return link or null with a small chance */
    public static Link of(RoboTaxi roboTaxi) {
        Schedule schedule = Objects.requireNonNull(roboTaxi.getSchedule());
        /** {@link ScheduleImpl.failIfNotStarted} triggers, very likely you have
         * entered a simulation start time other than 0:00. Check that in the
         * av_config.xml file. */
        return new RoboTaxiLocation(schedule.getCurrentTask()).link;
    }

    // ---
    /** DO NOT INITIALIZE THE LINK VARIABLE !!! */
    private Link link;

    private RoboTaxiLocation(Task task) {
        super(task);
    }

    @Override
    public void handle(AmodeusPickupTask avPickupTask) {
        link = avPickupTask.getLink();
    }

    @Override
    public void handle(AmodeusDropoffTask avDropoffTask) {
        link = avDropoffTask.getLink();
    }

    @Override
    public void handle(AmodeusDriveTask avDriveTask) {
        TaskTracker taskTracker = avDriveTask.getTaskTracker();
        OnlineDriveTaskTracker onlineDriveTaskTracker = (OnlineDriveTaskTracker) taskTracker;
        // there is a slim chance that function getDiversionPoint() returns null
        LinkTimePair linkTimePair = onlineDriveTaskTracker.getDiversionPoint();
        if (Objects.nonNull(linkTimePair))
            link = linkTimePair.link;
    }

    @Override
    public void handle(AmodeusStayTask avStayTask) {
        link = avStayTask.getLink();
    }
}
