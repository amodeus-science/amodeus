package amodeus.amodeus.dispatcher.shared.schedule;

import org.matsim.amodeus.config.modal.TimingConfig;
import org.matsim.amodeus.dvrp.schedule.AmodeusDriveTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask.StopType;
import org.matsim.amodeus.dvrp.schedule.AmodeusTaskTypes;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.schedule.DriveTask;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.StayTask;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.schedule.Task.TaskStatus;
import org.matsim.contrib.dvrp.tracker.OnlineDriveTaskTracker;
import org.matsim.contrib.dvrp.util.LinkTimePair;

import amodeus.amodeus.dispatcher.core.RoboTaxi;

public class ScheduleManager {
    private final Schedule schedule;
    private final TimingConfig timingConfig;
    private final FutureVrpPathCalculator router;

    public ScheduleManager(RoboTaxi vehicle, TimingConfig timingConfig, FutureVrpPathCalculator router) {
        this.schedule = vehicle.getSchedule();
        this.timingConfig = timingConfig;
        this.router = router;
    }

    private boolean isStop(Task task) {
        return AmodeusTaskTypes.STOP.equals(task.getTaskType());
    }

    private boolean isDrive(Task task) {
        return AmodeusTaskTypes.DRIVE.equals(task.getTaskType());
    }

    private boolean isStay(Task task) {
        return AmodeusTaskTypes.STAY.equals(task.getTaskType());
    }

    private boolean isRemovable(Task task) {
        if (task.getStatus() == TaskStatus.PLANNED) {
            return isDrive(task) || isStay(task);
        }

        return false;
    }

    private boolean isDivertable(Task task) {
        if (isDrive(task) && task.getStatus() != TaskStatus.PERFORMED) {
            if (task.getTaskTracker() instanceof OnlineDriveTaskTracker) {
                return ((OnlineDriveTaskTracker) task).getDiversionPoint() != null;
            }
        }

        return false;
    }

    private Link getEndLink(Task task) {
        if (task instanceof DriveTask) {
            return ((DriveTask) task).getPath().getToLink();
        } else if (task instanceof StayTask) {
            return ((StayTask) task).getLink();
        } else {
            throw new IllegalStateException("Task is neither DriveTask nor StayTask");
        }
    }

    /** If we add a pick-up or drop-off, we want to add it as soon as possible:
     * - We never want to delete existing pick up / drop off tasks
     * - We are happy to delete stay and drive tasks
     * - We can divert existing drive tasks */
    private Task removeTail() {
        int index = schedule.getTaskCount() - 1;

        while (isRemovable(schedule.getTasks().get(index))) {
            schedule.removeLastTask();
            index--;
        }

        return Schedules.getLastTask(schedule);
    }

    public void addPickup(PassengerRequest request) {
        addStop(request, true);
    }

    public void addDropoff(PassengerRequest request) {
        addStop(request, false);
    }

    protected void addStop(PassengerRequest request, boolean isPickup) {
        Task previousTask = removeTail();

        Link stopLink = isPickup ? request.getFromLink() : request.getToLink();
        AmodeusStopTask stopTask = null;

        if (isStop(previousTask)) {
            // If the schedule end is already at the stop location and there is a stop task, reuse it!
            stopTask = (AmodeusStopTask) previousTask;

        } else {
            // Vehicle is either at a different location or driving. In the latter case,
            // we may be able to divert the vehicle.

            if (isDivertable(previousTask)) {
                implementDiversion(previousTask, stopLink);
            } else {
                previousTask = createDrive(previousTask, stopLink);
                schedule.addTask(previousTask);
            }

            stopTask = new AmodeusStopTask( //
                    previousTask.getEndTime(), //
                    previousTask.getEndTime(), //
                    stopLink, //
                    StopType.Pickup //
            );

            schedule.addTask(stopTask);
        }

        // Second, add the person to the task

        if (stopTask.getLink().equals(stopLink)) {
            if (isPickup) {
                stopTask.addPickupRequest(request);
            } else {
                stopTask.addDropoffRequest(request);
            }
        }

        // Last, recalculate the expected duration of the stop

        double duration = 0.0;

        duration += stopTask.getPickupRequests().size() * timingConfig.getPickupDurationPerPassenger();
        duration += stopTask.getDropoffRequests().size() * timingConfig.getDropoffDurationPerPassenger();

        if (stopTask.getPickupRequests().size() > 0) {
            duration = Math.max(duration, timingConfig.getMinimumPickupDurationPerStop());
        }

        if (stopTask.getDropoffRequests().size() > 0) {
            duration = Math.max(duration, timingConfig.getMinimumDropoffDurationPerStop());
        }

        stopTask.setEndTime(stopTask.getBeginTime() + duration);

        // Last, add the final stay task

        schedule.addTask(createLastStay(previousTask));
    }

    public void addRequest(PassengerRequest request) {
        addPickup(request);
        addDropoff(request);
    }

    public void addDiversion(Link link) {
        Task previousTask = removeTail();

        if (isDivertable(previousTask)) {
            // If the vehicle is moving, we can divert it
            implementDiversion(previousTask, link);
        } else {
            previousTask = createDrive(previousTask, link);
            schedule.addTask(previousTask);
        }

        schedule.addTask(createLastStay(previousTask));
    }

    private Task createLastStay(Task previousTask) {
        double scheduleEndTime = schedule.getEndTime();

        if (previousTask.getEndTime() > scheduleEndTime) {
            throw new IllegalStateException();
        }

        return new AmodeusStayTask(previousTask.getEndTime(), scheduleEndTime, getEndLink(previousTask));
    }

    private void implementDiversion(Task task, Link destinationLink) {
        if (task.getStatus() == TaskStatus.STARTED) {
            throw new IllegalStateException("Can only implement diversions in running tasks");
        }

        OnlineDriveTaskTracker tracker = (OnlineDriveTaskTracker) task.getTaskTracker();
        LinkTimePair diversionPoint = tracker.getDiversionPoint();

        if (diversionPoint == null) {
            throw new IllegalStateException("Only call implementDiversion if you have checked that the vehicle is divertable.");
        }

        tracker.divertPath(router.calculatePath(diversionPoint.link, destinationLink, diversionPoint.time));
    }

    private Task createDrive(Task originTask, Link destinationLink) {
        Link originLink = getEndLink(originTask);
        double departureTime = originTask.getEndTime();

        return new AmodeusDriveTask(router.calculatePath(originLink, destinationLink, departureTime));
    }
}
