package ch.ethz.matsim.av.dispatcher.multi_od_heuristic;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.matsim.av.config.modal.TimingConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.multi_od_heuristic.aggregation.AggregatedRequest;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import ch.ethz.refactoring.schedule.AmodeusDriveTask;
import ch.ethz.refactoring.schedule.AmodeusDropoffTask;
import ch.ethz.refactoring.schedule.AmodeusPickupTask;
import ch.ethz.refactoring.schedule.AmodeusStayTask;

public class ParallelAggregateRideAppender implements AggregateRideAppender {
    final private ParallelLeastCostPathCalculator router;
    final private TravelTime travelTime;
    final private TimingConfig timing;
    final private TravelTimeEstimator travelTimeEstimator;
    private List<AppendTask> tasks = new LinkedList<>();

    public ParallelAggregateRideAppender(TimingConfig timing, ParallelLeastCostPathCalculator router, TravelTime travelTime, TravelTimeEstimator travelTimeEstimator) {
        this.router = router;
        this.travelTime = travelTime;
        this.timing = timing;
        this.travelTimeEstimator = travelTimeEstimator;
    }

    private class AppendTask {
        public AVVehicle vehicle;

        public List<AVRequest> pickupOrder = new LinkedList<>();
        public List<AVRequest> dropoffOrder = new LinkedList<>();

        public List<Future<Path>> pickupPaths = new LinkedList<>();
        public List<Future<Path>> dropoffPaths = new LinkedList<>();

        public double time;
    }

    private class OrderedRequest {
        final public double startTime;
        final public AVRequest request;

        public OrderedRequest(AVRequest request, double startTime) {
            this.startTime = startTime;
            this.request = request;
        }
    }

    public void schedule(AggregatedRequest request, AVVehicle vehicle, double now) {
        LinkedList<AVRequest> requests = new LinkedList<>();
        LinkedList<AVRequest> pickups = new LinkedList<>();
        LinkedList<AVRequest> dropoffs = new LinkedList<>();

        requests.addAll(request.getSlaveRequests());
        requests.add(request.getMasterRequest());
        pickups.addAll(request.getSlaveRequests());
        pickups.add(request.getMasterRequest());
        dropoffs.addAll(request.getSlaveRequests());
        dropoffs.add(request.getMasterRequest());

        LinkedList<OrderedRequest> pickupOrder = new LinkedList<>();
        LinkedList<OrderedRequest> dropoffOrder = new LinkedList<>();

        Schedule schedule = vehicle.getSchedule();
        AmodeusStayTask stayTask = (AmodeusStayTask) Schedules.getLastTask(schedule);

        Link currentLink = stayTask.getLink();
        double currentTime = now;

        while (pickups.size() > 0) {
            AVRequest closestRequest = null;
            double shortestTravelTime = Double.POSITIVE_INFINITY;

            for (AVRequest pickup : pickups) {
                double travelTime = travelTimeEstimator.estimateTravelTime(currentLink, pickup.getFromLink(), currentTime);

                if (travelTime < shortestTravelTime) {
                    closestRequest = pickup;
                    shortestTravelTime = travelTime;
                }
            }

            pickupOrder.add(new OrderedRequest(closestRequest, currentTime));
            pickups.remove(closestRequest);

            currentTime += shortestTravelTime + timing.getDropoffDurationPerPassenger();

            if (!currentLink.equals(closestRequest.getFromLink())) {
                currentTime += timing.getMinimumDropoffDurationPerStop();
            }

            currentLink = closestRequest.getFromLink();
        }

        while (dropoffs.size() > 0) {
            AVRequest closestRequest = null;
            double shortestTravelTime = Double.POSITIVE_INFINITY;

            for (AVRequest dropoff : dropoffs) {
                double travelTime = travelTimeEstimator.estimateTravelTime(currentLink, dropoff.getToLink(), currentTime);

                if (travelTime < shortestTravelTime) {
                    closestRequest = dropoff;
                    shortestTravelTime = travelTime;
                }
            }

            dropoffOrder.add(new OrderedRequest(closestRequest, currentTime));
            dropoffs.remove(closestRequest);

            currentTime += shortestTravelTime + timing.getDropoffDurationPerPassenger();

            if (!currentLink.equals(closestRequest.getToLink())) {
                currentTime += timing.getMinimumDropoffDurationPerStop();
            }

            currentLink = closestRequest.getToLink();
        }

        AppendTask appendTask = new AppendTask();
        appendTask.vehicle = vehicle;
        appendTask.time = now;

        appendTask.pickupOrder = pickupOrder.stream().map(o -> o.request).collect(Collectors.toList());
        appendTask.dropoffOrder = dropoffOrder.stream().map(o -> o.request).collect(Collectors.toList());

        // All elements are ordered... now schedule the routing

        currentTime = now;
        currentLink = stayTask.getLink();

        for (OrderedRequest pickup : pickupOrder) {
            if (!pickup.request.getFromLink().equals(currentLink)) {
                appendTask.pickupPaths.add(router.calcLeastCostPath(currentLink.getToNode(), pickup.request.getFromLink().getFromNode(), pickup.startTime, null, null));
                currentLink = pickup.request.getFromLink();
            } else {
                appendTask.pickupPaths.add(null);
            }
        }

        for (OrderedRequest dropoff : dropoffOrder) {
            if (!dropoff.request.getToLink().equals(currentLink)) {
                appendTask.dropoffPaths.add(router.calcLeastCostPath(currentLink.getToNode(), dropoff.request.getToLink().getFromNode(), dropoff.startTime, null, null));
                currentLink = dropoff.request.getToLink();
            } else {
                appendTask.dropoffPaths.add(null);
            }
        }

        tasks.add(appendTask);
    }

    public void schedule(AppendTask appendTask, List<Path> plainPickupPaths, List<Path> plainDropoffPaths) {
        Schedule schedule = appendTask.vehicle.getSchedule();
        AmodeusStayTask stayTask = (AmodeusStayTask) Schedules.getLastTask(schedule);

        double startTime = 0.0;
        double scheduleEndTime = schedule.getEndTime();

        if (stayTask.getStatus() == Task.TaskStatus.STARTED) {
            startTime = appendTask.time;
            stayTask.setEndTime(startTime);
        } else {
            startTime = stayTask.getBeginTime();
            schedule.removeLastTask();
        }

        Link currentLink = stayTask.getLink();
        double currentTime = startTime;

        Task currentTask = stayTask;
        LinkedList<AVRequest> currentRequests = new LinkedList<>();

        LinkedList<VrpPathWithTravelData> paths = new LinkedList<>();
        LinkedList<AmodeusDriveTask> driveTasks = new LinkedList<>();

        Iterator<Path> pickupPathIterator = plainPickupPaths.iterator();
        Iterator<Path> dropoffPathIterator = plainDropoffPaths.iterator();

        for (AVRequest customerRequest : appendTask.pickupOrder) {
            // REFACTOR: We are reconstructing the distances here, while there should already be a planned distance in the route depending on whether we predict it or not.
            // If we don't predict a distance, the scoring part should be able to cope with that. Certainly, summing up the *actual* driven distance as here, we can also do
            // (more easily) from the events. What we're interested in actually is the planned distance, which should be passed by the AVTransit event anyways. Need to find
            // a mechanism for that. Because this will not work anyways with Amodeus dispatchers!
            customerRequest.getRoute().setDistance(0.0);
        }

        for (AVRequest pickup : appendTask.pickupOrder) {
            Path plainPickupPath = pickupPathIterator.next();

            if (plainPickupPath != null) {
                VrpPathWithTravelData path = VrpPaths.createPath(currentLink, pickup.getFromLink(), currentTime, plainPickupPath, travelTime);
                paths.add(path);

                AmodeusDriveTask driveTask = new AmodeusDriveTask(path, currentRequests);
                driveTasks.add(driveTask);
                schedule.addTask(driveTask);

                currentTask = driveTask;
                currentLink = pickup.getFromLink();
                currentTime = path.getArrivalTime();

                double driveDistance = VrpPaths.calcDistance(path);

                for (AVRequest customerRequest : currentRequests) {
                    // REFACTOR
                    customerRequest.getRoute().setDistance(customerRequest.getRoute().getDistance() + driveDistance);
                }
            }

            if (currentTask instanceof AmodeusPickupTask) {
                ((AmodeusPickupTask) currentTask).addRequest(pickup);
                currentRequests.add(pickup);
            } else {
                AmodeusPickupTask pickupTask = new AmodeusPickupTask(currentTime, currentTime + timing.getMinimumPickupDurationPerStop(), pickup.getFromLink(), Double.NEGATIVE_INFINITY);
                pickupTask.addRequest(pickup);

                schedule.addTask(pickupTask);
                currentTask = pickupTask;
                currentRequests.add(pickup);
                currentTime += timing.getMinimumPickupDurationPerStop();
            }
        }

        for (AVRequest dropoff : appendTask.dropoffOrder) {
            Path plainDropoffPath = dropoffPathIterator.next();

            if (plainDropoffPath != null) {
                VrpPathWithTravelData path = VrpPaths.createPath(currentLink, dropoff.getToLink(), currentTime, plainDropoffPath, travelTime);
                paths.add(path);

                AmodeusDriveTask driveTask = new AmodeusDriveTask(path, currentRequests);
                driveTasks.add(driveTask);
                schedule.addTask(driveTask);

                currentTask = driveTask;
                currentLink = dropoff.getToLink();
                currentTime = path.getArrivalTime();

                double driveDistance = VrpPaths.calcDistance(path);

                for (AVRequest customerRequest : currentRequests) {
                    // REFACTOR
                    customerRequest.getRoute().setDistance(customerRequest.getRoute().getDistance() + driveDistance);
                }
            }

            if (currentTask instanceof AmodeusDropoffTask) {
                ((AmodeusDropoffTask) currentTask).addRequest(dropoff);
                currentRequests.remove(dropoff);
            } else {
                AmodeusDropoffTask dropoffTask = new AmodeusDropoffTask(currentTime, currentTime + timing.getMinimumDropoffDurationPerStop(), dropoff.getToLink());
                dropoffTask.addRequest(dropoff);

                schedule.addTask(dropoffTask);
                currentTask = dropoffTask;
                currentRequests.remove(dropoff);
                currentTime += timing.getMinimumDropoffDurationPerStop();
            }
        }

        if (currentTask.getEndTime() < scheduleEndTime) {
            schedule.addTask(new AmodeusStayTask(currentTime, scheduleEndTime, currentLink));
        }
    }

    public void update() {
        try {
            for (AppendTask task : tasks) {
                List<Path> plainPickupTasks = new LinkedList<>();
                List<Path> plainDropoffTasks = new LinkedList<>();

                for (Future<Path> future : task.pickupPaths) {
                    plainPickupTasks.add(future == null ? null : future.get());
                }

                for (Future<Path> future : task.dropoffPaths) {
                    plainDropoffTasks.add(future == null ? null : future.get());
                }

                schedule(task, plainPickupTasks, plainDropoffTasks);
            }

            tasks.clear();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
