package amodeus.amodeus.dispatcher.core.schedule;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask.StopType;
import org.matsim.amodeus.dvrp.schedule.AmodeusTaskTypes;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.schedule.DrtDriveTask;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.drt.schedule.DrtStopTask;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.StayTask;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.schedule.Task.TaskStatus;
import org.matsim.contrib.dvrp.tracker.OnlineDriveTaskTracker;
import org.matsim.contrib.dvrp.util.LinkTimePair;

import com.google.common.collect.ImmutableList;

import amodeus.amodeus.dispatcher.core.RoboTaxi;

public class ScheduleManager {
    private final Schedule schedule;
    private final FutureVrpPathCalculator router;

    private final List<InternalStop> stopSequence = new LinkedList<>();
    private final Set<PassengerRequest> onboard = new HashSet<>();
    private Link destinationLink;

    private double now = 0.0;

    private class InternalStop implements Stop {
        private final PassengerRequest request;
        private final boolean isPickup;
        private AmodeusStopTask task;

        InternalStop(PassengerRequest request, boolean isPickup) {
            this.request = request;
            this.isPickup = isPickup;
        }

        void setTask(AmodeusStopTask task) {
            this.task = task;
        }

        AmodeusStopTask getTask() {
            return task;
        }

        @Override
        public PassengerRequest getRequest() {
            return request;
        }

        @Override
        public boolean isPickup() {
            return isPickup;
        }

        @Override
        public boolean isModifiable() {
            return task.getStatus() == TaskStatus.PLANNED;
        }
    }

    public ScheduleManager(RoboTaxi vehicle, FutureVrpPathCalculator router) {
        this.schedule = vehicle.getSchedule();
        this.router = router;
    }

    private boolean isStop(Task task) {
        return AmodeusTaskTypes.STOP.equals(task.getTaskType());
    }

    private boolean isDrive(Task task) {
        return DrtDriveTask.TYPE.equals(task.getTaskType());
    }

    private boolean isStay(Task task) {
        return DrtStayTask.TYPE.equals(task.getTaskType());
    }

    public void updateSequence(double now) {
        this.now = now;
        Iterator<InternalStop> iterator = stopSequence.iterator();

        while (iterator.hasNext()) {
            InternalStop stop = iterator.next();

            if (stop.task == null) {
                if (stop.task.getStatus() != TaskStatus.PLANNED) {
                    if (stop.isPickup()) {
                        boolean wasPresent = !onboard.add(stop.getRequest());

                        if (wasPresent) {
                            throw new IllegalStateException("Request is already on board");
                        }
                    } else {
                        boolean wasPresent = onboard.remove(stop.getRequest());

                        if (!wasPresent) {
                            throw new IllegalStateException("Request is not on board");
                        }
                    }
                }

                if (stop.task.getStatus() == TaskStatus.PERFORMED) {
                    iterator.remove();
                }
            }
        }
    }

    private void updateSchedule() {
        Task currentTask = schedule.getCurrentTask();

        // Clean up schedule

        int currentIndex = currentTask.getTaskIdx();

        while (Schedules.getLastTask(schedule).getTaskIdx() > currentIndex) {
            schedule.removeLastTask();
        }

        List<InternalStop> sequence = new LinkedList<>(stopSequence);

        if (sequence.size() > 0) {
            if (isStop(currentTask)) {
                // Skip all stops in the sequence which are handled by the current task
                DrtStopTask stopTask = (DrtStopTask) currentTask;

                while (sequence.size() > 0) {
                    InternalStop stop = sequence.get(0);

                    if (stop.isPickup() && stopTask.getPickupRequests().containsKey(stop.getRequest().getId())) {
                        sequence.remove(0);
                        continue;
                    }

                    if (!stop.isPickup() && stopTask.getDropoffRequests().containsKey(stop.getRequest().getId())) {
                        sequence.remove(0);
                        continue;
                    }

                    break;
                }
            } else if (isDrive(currentTask)) {
                // If driving, make sure we're driving to the first stop. If needed, divert.
                InternalStop stop = sequence.get(0);
                Link stopLink = stop.isPickup() ? stop.getRequest().getFromLink() : stop.getRequest().getToLink();

                DrtDriveTask driveTask = (DrtDriveTask) currentTask;

                if (stopLink != driveTask.getPath().getToLink()) {
                    OnlineDriveTaskTracker tracker = (OnlineDriveTaskTracker) driveTask.getTaskTracker();
                    LinkTimePair diversionPoint = tracker.getDiversionPoint();

                    VrpPathWithTravelData path = router.calculatePath(diversionPoint.link, stopLink, diversionPoint.time);
                    tracker.divertPath(path);
                }
            } else if (isStay(currentTask)) {
                // If current is a stay task, stop it
                DrtStayTask stayTask = (DrtStayTask) currentTask;
                stayTask.setEndTime(now);
            }

            // Now, rebuild the schedule based on the stop sequence

            Task previousTask = currentTask;

            for (InternalStop stop : sequence) {
                double previousEndTime = previousTask.getEndTime();
                Link previousLink = null;

                if (isDrive(previousTask)) {
                    previousLink = ((DrtDriveTask) previousTask).getPath().getToLink();
                } else if (isStay(previousTask)) {
                    previousLink = ((DrtStayTask) previousTask).getLink();
                } else if (isStop(previousTask)) {
                    previousLink = ((AmodeusStopTask) previousTask).getLink();
                }

                Link stopLink = stop.isPickup() ? stop.getRequest().getFromLink() : stop.getRequest().getToLink();

                if (stopLink != previousLink) {
                    // We need to add a drive in between

                    VrpPathWithTravelData path = router.calculatePath(previousLink, stopLink, previousEndTime);
                    DrtDriveTask driveTask = new DrtDriveTask(path, DrtDriveTask.TYPE);
                    schedule.addTask(driveTask);

                    previousEndTime = driveTask.getEndTime();
                    previousLink = driveTask.getPath().getToLink();
                    previousTask = driveTask;
                }

                boolean addStopTask = false;

                // TODO: Simplify with generalized StopTask instead of StopType discrimination

                if (isStop(previousTask)) {
                    // If we're already at the stop, so we can add the passenger
                    AmodeusStopTask stopTask = (AmodeusStopTask) previousTask;

                    if (stop.isPickup() && stopTask.getStopType() == StopType.Pickup) {
                        stopTask.addPickupRequest(stop.getRequest());
                        stop.task = stopTask;
                    } else if (!stop.isPickup() && stopTask.getStopType() == StopType.Dropoff) {
                        stopTask.addDropoffRequest(stop.getRequest());
                        stop.task = stopTask;
                    } else {
                        addStopTask = true;
                    }

                    // TODO: Can it happen that we pick up and drop off a customer at the same stop? In that case, we can handle this special case here.
                }

                if (addStopTask) {
                    // We need to add a new stop task

                    double ESTIMATED_STOP_TIME = 600.0; // TODO: We don't really care.

                    AmodeusStopTask task = new AmodeusStopTask(now, now + ESTIMATED_STOP_TIME, stopLink, stop.isPickup() ? StopType.Pickup : StopType.Dropoff);
                    stop.task = task;
                    schedule.addTask(task);

                    previousTask = task;
                }
            }

            // Finally, add a diversion if requested

            if (destinationLink != null) {
                DrtStopTask stopTask = (DrtStopTask) previousTask;

                if (stopTask.getLink() != destinationLink) {
                    VrpPathWithTravelData path = router.calculatePath(stopTask.getLink(), destinationLink, stopTask.getEndTime());
                    DrtDriveTask driveTask = new DrtDriveTask(path, DrtDriveTask.TYPE);
                    schedule.addTask(driveTask);
                }
            }
        } else { // There were not stops in the sequence
            if (destinationLink != null) { // But we may want to add a diversion
                if (isDrive(currentTask)) {
                    DrtDriveTask driveTask = (DrtDriveTask) currentTask;

                    if (driveTask.getPath().getToLink() != destinationLink) {
                        // We need a diversion

                        OnlineDriveTaskTracker tracker = (OnlineDriveTaskTracker) driveTask.getTaskTracker();
                        LinkTimePair diversionPoint = tracker.getDiversionPoint();

                        VrpPathWithTravelData path = router.calculatePath(diversionPoint.link, destinationLink, diversionPoint.time);
                        tracker.divertPath(path);
                    }
                } else {
                    StayTask stayTask = (StayTask) currentTask;

                    if (stayTask.getLink() != destinationLink) {
                        // We need to add a new task

                        VrpPathWithTravelData path = router.calculatePath(stayTask.getLink(), destinationLink, stayTask.getEndTime());
                        DrtDriveTask driveTask = new DrtDriveTask(path, DrtDriveTask.TYPE);
                        schedule.addTask(driveTask);
                    }
                }
            }
        }

        // At the end, add the inifinite stay task (TODO: Avoid that later on with schedule resitriction!)

        Task lastTask = Schedules.getLastTask(schedule);

        if (!isStay(lastTask)) {
            Link lastLink = null;

            if (isDrive(lastTask)) {
                lastLink = ((DrtDriveTask) lastTask).getPath().getToLink();
            } else {
                lastLink = ((StayTask) lastTask).getLink();
            }

            DrtStayTask stayTask = new DrtStayTask(lastTask.getEndTime(), Double.POSITIVE_INFINITY, lastLink);
            schedule.addTask(stayTask);
        }
    }

    public void addRequest(PassengerRequest request) {
        for (InternalStop stop : stopSequence) {
            if (stop.getRequest() == request) {
                throw new IllegalStateException("Request is already registered");
            }
        }

        stopSequence.add(new InternalStop(request, true));
        stopSequence.add(new InternalStop(request, false));

        updateSchedule();
    }

    public void removeRequest(PassengerRequest request) {
        Iterator<InternalStop> iterator = stopSequence.iterator();

        Task currentTask = schedule.getCurrentTask();

        if (isStay(currentTask)) {
            AmodeusStopTask stopTask = (AmodeusStopTask) currentTask;

            if (stopTask.getPickupRequests().containsKey(request.getId())) {
                throw new IllegalStateException("Pick-up is already ongoing");
            }
        }

        if (onboard.contains(request)) {
            throw new IllegalStateException("Passenger is already on board");
        }

        while (iterator.hasNext()) {
            InternalStop stop = iterator.next();

            if (stop.getRequest() == request) {
                iterator.remove();
            }
        }

        updateSchedule();
    }

    public ImmutableList<Stop> getStopSequence() {
        return ImmutableList.copyOf(stopSequence.stream().map(s -> new DefaultStop(s.getRequest(), s.isPickup(), s.isModifiable())).collect(Collectors.toList()));
    }

    public void setStopSequence(ImmutableList<Stop> sequence) {
        // All stops that are currently handled need to be replicated exactly!

        int index = 0;

        while (stopSequence.get(index).task.getStatus() != TaskStatus.PLANNED) {

            if (!stopSequence.get(index).equals(sequence.get(index))) {
                throw new IllegalStateException("Element " + index + " can not be changed anymore!");
            }

            index++;
        }

        // Check sequence of pickups and dropoffs

        Set<PassengerRequest> pickups = new HashSet<>();

        for (Stop stop : sequence) {
            if (stop.isPickup()) {
                boolean wasRegistered = !pickups.add(stop.getRequest());

                if (wasRegistered) {
                    throw new IllegalStateException("Pick-up added twice");
                }
            } else {
                boolean wasRegistered = pickups.remove(stop.getRequest());

                if (!wasRegistered) {
                    throw new IllegalStateException("Pick-up was not registered");
                }
            }
        }

        // Update internal sequence

        while (stopSequence.size() >= index) {
            stopSequence.remove(stopSequence.size() - 1);
        }

        for (int i = index; i < sequence.size(); i++) {
            Stop stop = sequence.get(i);
            stopSequence.add(new InternalStop(stop.getRequest(), stop.isPickup()));
        }

        if (stopSequence.size() != sequence.size()) {
            throw new IllegalStateException("Sequences must have same length");
        }

        updateSchedule();
    }

    public static int findIndex(ImmutableList<Stop> sequence, PassengerRequest request, boolean findPickup) {
        for (int index = 0; index < sequence.size(); index++) {
            Stop stop = sequence.get(index);

            if (findPickup == stop.isPickup() && request == stop.getRequest()) {
                return index;
            }
        }

        throw new IllegalStateException("Request not found");
    }

    public static Stop findStop(ImmutableList<Stop> sequence, PassengerRequest request, boolean findPickup) {
        int index = findIndex(sequence, request, findPickup);
        return sequence.get(index);
    }
}
