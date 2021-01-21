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
import com.google.common.collect.ImmutableSet;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.schedule.directives.DefaultDriveDirective;
import amodeus.amodeus.dispatcher.core.schedule.directives.DefaultStopDirective;
import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.DriveDirective;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

public class ScheduleManager {
    private final Schedule schedule;
    private final FutureVrpPathCalculator router;

    private final List<InternalDirective> directiveSequence = new LinkedList<>();
    private final Set<PassengerRequest> onboard = new HashSet<>();

    private double now = 0.0;

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
        Iterator<InternalDirective> iterator = directiveSequence.iterator();

        while (iterator.hasNext()) {
            InternalDirective directive = iterator.next();

            if (directive.hasTask()) {
                if (directive.getTask().getStatus() == TaskStatus.PERFORMED) {
                    if (directive instanceof StopDirective) {
                        StopDirective stopDirective = (StopDirective) directive;

                        if (stopDirective.isPickup()) {
                            boolean wasPresent = !onboard.add(stopDirective.getRequest());

                            if (wasPresent) {
                                throw new IllegalStateException("Request is already on board");
                            }
                        } else {
                            boolean wasPresent = onboard.remove(stopDirective.getRequest());

                            if (!wasPresent) {
                                throw new IllegalStateException("Request is not on board");
                            }
                        }
                    }

                    iterator.remove();
                } else {
                    break;
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

        List<InternalDirective> sequence = new LinkedList<>(directiveSequence);

        if (sequence.size() > 0) {
            if (isStop(currentTask)) {
                // Skip all stops in the sequence which are handled by the current task
                AmodeusStopTask stopTask = (AmodeusStopTask) currentTask;

                while (sequence.size() > 0) {
                    InternalDirective directive = sequence.get(0);

                    if (directive instanceof StopDirective) {
                        StopDirective stopDirective = (StopDirective) directive;

                        if (stopDirective.isPickup() && stopTask.getPickupRequests().containsKey(stopDirective.getRequest().getId())) {
                            sequence.remove(0);
                            continue;
                        }

                        if (!stopDirective.isPickup() && stopTask.getDropoffRequests().containsKey(stopDirective.getRequest().getId())) {
                            sequence.remove(0);
                            continue;
                        }
                    }

                    break;
                }
            } else if (isDrive(currentTask)) {
                // If driving, make sure we're driving to the first stop. If needed, divert.
                Directive directive = sequence.get(0);
                Link destination = null;

                if (directive instanceof DriveDirective) {
                    InternalDriveDirective driveDirective = (InternalDriveDirective) directive;
                    destination = driveDirective.getDestination();
                    driveDirective.setTask(currentTask);
                    sequence.remove(0);
                } else {
                    InternalStopDirective stopDirective = (InternalStopDirective) directive;
                    destination = stopDirective.isPickup ? stopDirective.getRequest().getFromLink() : stopDirective.getRequest().getToLink();
                }

                DrtDriveTask driveTask = (DrtDriveTask) currentTask;

                if (destination != driveTask.getPath().getToLink()) {
                    OnlineDriveTaskTracker tracker = (OnlineDriveTaskTracker) driveTask.getTaskTracker();
                    LinkTimePair diversionPoint = tracker.getDiversionPoint();

                    VrpPathWithTravelData path = router.calculatePath(diversionPoint.link, destination, diversionPoint.time);
                    tracker.divertPath(path);
                }
            } else if (isStay(currentTask)) {
                // If current is a stay task, stop it
                DrtStayTask stayTask = (DrtStayTask) currentTask;
                stayTask.setEndTime(now);
            }

            // Now, rebuild the schedule based on the stop sequence

            Task previousTask = currentTask;

            for (InternalDirective directive : sequence) {
                double previousEndTime = previousTask.getEndTime();
                Link previousLink = null;

                if (isDrive(previousTask)) {
                    previousLink = ((DrtDriveTask) previousTask).getPath().getToLink();
                } else if (isStay(previousTask)) {
                    previousLink = ((DrtStayTask) previousTask).getLink();
                } else if (isStop(previousTask)) {
                    previousLink = ((AmodeusStopTask) previousTask).getLink();
                }

                if (directive instanceof StopDirective) {
                    InternalStopDirective stopDirective = (InternalStopDirective) directive;
                    Link stopLink = stopDirective.isPickup() ? stopDirective.getRequest().getFromLink() : stopDirective.getRequest().getToLink();

                    if (stopLink != previousLink) {
                        // We need to add a drive in between

                        VrpPathWithTravelData path = router.calculatePath(previousLink, stopLink, previousEndTime);
                        DrtDriveTask driveTask = new DrtDriveTask(path, DrtDriveTask.TYPE);
                        schedule.addTask(driveTask);

                        previousEndTime = driveTask.getEndTime();
                        previousLink = driveTask.getPath().getToLink();
                        previousTask = driveTask;
                    }

                    boolean addStopTask = true;

                    // TODO: Simplify with generalized StopTask instead of StopType discrimination

                    if (isStop(previousTask) && previousTask != currentTask) {
                        // If we're already at the stop, so we can add the passenger
                        AmodeusStopTask stopTask = (AmodeusStopTask) previousTask;

                        if (stopDirective.isPickup() && stopTask.getStopType() == StopType.Pickup) {
                            stopTask.addPickupRequest(stopDirective.getRequest());
                            stopDirective.setTask(stopTask);
                            addStopTask = false;
                        } else if (!stopDirective.isPickup() && stopTask.getStopType() == StopType.Dropoff) {
                            stopTask.addDropoffRequest(stopDirective.getRequest());
                            stopDirective.setTask(stopTask);
                            addStopTask = false;
                        }

                        // TODO: Can it happen that we pick up and drop off a customer at the same stop? In that case, we can handle this special case here.
                    }

                    if (addStopTask) {
                        // We need to add a new stop task

                        double ESTIMATED_STOP_TIME = 600.0; // TODO: We don't really care.

                        AmodeusStopTask task = new AmodeusStopTask(previousEndTime, previousEndTime + ESTIMATED_STOP_TIME, stopLink,
                                stopDirective.isPickup() ? StopType.Pickup : StopType.Dropoff);

                        if (stopDirective.isPickup()) {
                            task.addPickupRequest(stopDirective.getRequest());
                        } else {
                            task.addDropoffRequest(stopDirective.getRequest());
                        }

                        stopDirective.setTask(task);
                        schedule.addTask(task);

                        previousTask = task;
                    }
                } else {
                    InternalDriveDirective driveDirective = (InternalDriveDirective) directive;
                    Link destination = driveDirective.getDestination();

                    // We need to add a drive in between

                    VrpPathWithTravelData path = router.calculatePath(previousLink, destination, previousEndTime);
                    DrtDriveTask driveTask = new DrtDriveTask(path, DrtDriveTask.TYPE);
                    driveDirective.setTask(driveTask);
                    schedule.addTask(driveTask);

                    previousEndTime = driveTask.getEndTime();
                    previousLink = driveTask.getPath().getToLink();
                    previousTask = driveTask;
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

        for (InternalDirective directive : directiveSequence) {
            if (!directive.hasTask()) {
                throw new IllegalStateException("Found directive without task!");
            }
        }
    }

    private List<InternalStopDirective> getStopDirectives() {
        return directiveSequence.stream().filter(InternalStopDirective.class::isInstance).map(InternalStopDirective.class::cast).collect(Collectors.toList());
    }

    public void addRequest(PassengerRequest request) {
        List<Directive> directives = new LinkedList<>(getDirectives());

        directives.add(new InternalStopDirective(request, true));
        directives.add(new InternalStopDirective(request, false));

        setDirectives(directives);
    }

    public void removeRequest(PassengerRequest request) {
        List<Directive> directives = new LinkedList<>(getDirectives());
        Iterator<Directive> iterator = directives.iterator();

        while (iterator.hasNext()) {
            Directive directive = iterator.next();

            if (directive instanceof StopDirective) {
                StopDirective stopDirective = (StopDirective) directive;

                if (stopDirective.getRequest().equals(request)) {
                    iterator.remove();
                }
            }
        }

        setDirectives(directives);
        updateSchedule();
    }

    public ImmutableList<Directive> getDirectives() {
        ImmutableList.Builder<Directive> builder = new ImmutableList.Builder<>();

        for (InternalDirective directive : directiveSequence) {
            if (directive instanceof StopDirective) {
                StopDirective stopDirective = (StopDirective) directive;
                builder.add(new DefaultStopDirective(stopDirective.getRequest(), stopDirective.isPickup(), stopDirective.isModifiable()));
            } else {
                DriveDirective driveDirective = (DriveDirective) directive;
                builder.add(new DefaultDriveDirective(driveDirective.getDestination(), driveDirective.isModifiable()));
            }
        }

        return builder.build();
    }

    public void setDirectives(List<? extends Directive> sequence) {
        // All stops that are currently handled need to be replicated exactly!

        /* Set<String> ids = new HashSet<>();
         * 
         * for (Directive directive : sequence) {
         * if (directive instanceof StopDirective) {
         * StopDirective stopDirective = (StopDirective) directive;
         * ids.add(stopDirective.getRequest().getId().toString());
         * }
         * }
         * 
         * System.err.println("    setDirectives with " + String.join(", ", ids)); */

        int index = 0;

        if (directiveSequence.size() > 0) {
            while (index < directiveSequence.size() && !directiveSequence.get(index).isModifiable()) {
                if (sequence.size() <= index) {
                    throw new IllegalStateException("Element " + index + " can not be deleted anymore!");
                }

                if (!directiveSequence.get(index).isEqual(sequence.get(index))) {
                    throw new IllegalStateException("Element " + index + " can not be changed anymore!");
                }

                index++;
            }
        }

        // Check sequence of pickups and dropoffs

        Set<PassengerRequest> pickups = new HashSet<>(onboard);

        for (Directive directive : sequence) {
            if (directive instanceof StopDirective) {
                StopDirective stopDirective = (StopDirective) directive;

                if (stopDirective.isPickup()) {
                    boolean wasRegistered = !pickups.add(stopDirective.getRequest());

                    if (wasRegistered) {
                        throw new IllegalStateException("Pick-up added twice");
                    }
                } else {
                    boolean wasRegistered = pickups.remove(stopDirective.getRequest());

                    if (!wasRegistered) {
                        throw new IllegalStateException("Pick-up was not registered");
                    }
                }
            }
        }

        if (pickups.size() > 0) {
            throw new IllegalStateException("Some pick-ups do not have a drop-off");
        }

        // Update internal sequence

        while (directiveSequence.size() > index) {
            directiveSequence.remove(directiveSequence.size() - 1);
        }

        for (int i = index; i < sequence.size(); i++) {
            Directive directive = sequence.get(i);

            if (directive instanceof StopDirective) {
                StopDirective stopDirective = (StopDirective) directive;
                directiveSequence.add(new InternalStopDirective(stopDirective.getRequest(), stopDirective.isPickup()));
            } else {
                DriveDirective driveDirective = (DriveDirective) directive;
                directiveSequence.add(new InternalDriveDirective(driveDirective.getDestination()));
            }
        }

        if (directiveSequence.size() != sequence.size()) {
            throw new IllegalStateException("Sequences must have same length");
        }

        updateSchedule();
    }

    public static int findIndex(List<Directive> sequence, PassengerRequest request, boolean findPickup) {
        for (int index = 0; index < sequence.size(); index++) {
            Directive directive = sequence.get(index);

            if (directive instanceof StopDirective) {
                StopDirective stopDirective = (StopDirective) directive;

                if (findPickup == stopDirective.isPickup() && request == stopDirective.getRequest()) {
                    return index;
                }
            }
        }

        throw new IllegalStateException("Request not found");
    }

    public static Directive findStop(List<Directive> sequence, PassengerRequest request, boolean findPickup) {
        int index = findIndex(sequence, request, findPickup);
        return sequence.get(index);
    }

    public ImmutableSet<PassengerRequest> getOnBoardRequests() {
        return ImmutableSet.copyOf(onboard);
    }

    public int getNumberOfOnBoardRequests() {
        return onboard.size();
    }

    public boolean isTopModifiable() {
        return directiveSequence.size() == 0 || directiveSequence.get(0).isModifiable();
    }

    private interface InternalDirective extends Directive {
        void setTask(Task task);

        Task getTask();

        boolean hasTask();

        boolean isEqual(Directive other);
    }

    private class InternalStopDirective implements StopDirective, InternalDirective {
        private final PassengerRequest request;
        private final boolean isPickup;
        private AmodeusStopTask task;

        InternalStopDirective(PassengerRequest request, boolean isPickup) {
            this.request = request;
            this.isPickup = isPickup;
        }

        @Override
        public void setTask(Task task) {
            if (!(task instanceof AmodeusStopTask)) {
                throw new IllegalStateException("Expected AmodeusStopTask");
            }

            this.task = (AmodeusStopTask) task;
        }

        public AmodeusStopTask getTask() {
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

        @Override
        public boolean hasTask() {
            return task != null;
        }

        @Override
        public boolean isEqual(Directive other) {
            if (other instanceof StopDirective) {
                StopDirective stopDirective = (StopDirective) other;

                boolean isEqual = true;
                isEqual &= request.getId().equals(stopDirective.getRequest().getId());
                isEqual &= isPickup == stopDirective.isPickup();

                return isEqual;
            }

            return false;
        }

        @Override
        public String toString() {
            return "InternalStopDirective[" + request.getId() + ", " + (isPickup ? "Pickup" : "Dropoff") + ", " + (isModifiable() ? "Modifiable" : "Not modifiable") + "]";
        }
    }

    private class InternalDriveDirective implements DriveDirective, InternalDirective {
        private final Link destination;
        private DrtDriveTask task;

        InternalDriveDirective(Link destination) {
            this.destination = destination;
        }

        @Override
        public void setTask(Task task) {
            if (!(task instanceof DrtDriveTask)) {
                throw new IllegalStateException("Expected DrtDriveTask");
            }

            this.task = (DrtDriveTask) task;
        }

        @Override
        public DrtDriveTask getTask() {
            return task;
        }

        @Override
        public Link getDestination() {
            return destination;
        }

        @Override
        public boolean isModifiable() {
            if (task.getStatus() == TaskStatus.PLANNED) {
                return true;
            } else if (task.getStatus() == TaskStatus.STARTED) {
                return ((OnlineDriveTaskTracker) task.getTaskTracker()).getDiversionPoint() != null;
            } else {
                return false;
            }
        }

        @Override
        public boolean hasTask() {
            return task != null;
        }

        @Override
        public boolean isEqual(Directive other) {
            if (other instanceof DriveDirective) {
                DriveDirective driveDirective = (DriveDirective) other;

                boolean isEqual = true;
                isEqual &= destination.equals(driveDirective.getDestination());
                return isEqual;
            }

            return false;
        }

        @Override
        public String toString() {
            return "InternalDriveDirective[" + destination.getId() + ", " + (isModifiable() ? "Modifiable" : "Not modifiable") + "]";
        }
    }
}
