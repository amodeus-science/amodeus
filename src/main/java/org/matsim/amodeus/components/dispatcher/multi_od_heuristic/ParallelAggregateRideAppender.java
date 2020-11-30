package org.matsim.amodeus.components.dispatcher.multi_od_heuristic;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.matsim.amodeus.components.dispatcher.multi_od_heuristic.aggregation.AggregatedRequest;
import org.matsim.amodeus.config.modal.TimingConfig;
import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask.StopType;
import org.matsim.amodeus.plpc.ParallelLeastCostPathCalculator;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.schedule.DrtDriveTask;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelTime;

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
        public DvrpVehicle vehicle;

        public List<PassengerRequest> pickupOrder = new LinkedList<>();
        public List<PassengerRequest> dropoffOrder = new LinkedList<>();

        public List<Future<Path>> pickupPaths = new LinkedList<>();
        public List<Future<Path>> dropoffPaths = new LinkedList<>();

        public double time;
    }

    private class OrderedRequest {
        final public double startTime;
        final public PassengerRequest request;

        public OrderedRequest(PassengerRequest request, double startTime) {
            this.startTime = startTime;
            this.request = request;
        }
    }

    public void schedule(AggregatedRequest request, DvrpVehicle vehicle, double now) {
        LinkedList<PassengerRequest> requests = new LinkedList<>();
        LinkedList<PassengerRequest> pickups = new LinkedList<>();
        LinkedList<PassengerRequest> dropoffs = new LinkedList<>();

        requests.addAll(request.getSlaveRequests());
        requests.add(request.getMasterRequest());
        pickups.addAll(request.getSlaveRequests());
        pickups.add(request.getMasterRequest());
        dropoffs.addAll(request.getSlaveRequests());
        dropoffs.add(request.getMasterRequest());

        LinkedList<OrderedRequest> pickupOrder = new LinkedList<>();
        LinkedList<OrderedRequest> dropoffOrder = new LinkedList<>();

        Schedule schedule = vehicle.getSchedule();
        DrtStayTask stayTask = (DrtStayTask) Schedules.getLastTask(schedule);

        Link currentLink = stayTask.getLink();
        double currentTime = now;

        while (pickups.size() > 0) {
            PassengerRequest closestRequest = null;
            double shortestTravelTime = Double.POSITIVE_INFINITY;

            for (PassengerRequest pickup : pickups) {
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
            PassengerRequest closestRequest = null;
            double shortestTravelTime = Double.POSITIVE_INFINITY;

            for (PassengerRequest dropoff : dropoffs) {
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
        DrtStayTask stayTask = (DrtStayTask) Schedules.getLastTask(schedule);

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
        LinkedList<PassengerRequest> currentRequests = new LinkedList<>();

        LinkedList<VrpPathWithTravelData> paths = new LinkedList<>();
        LinkedList<DrtDriveTask> driveTasks = new LinkedList<>();

        Iterator<Path> pickupPathIterator = plainPickupPaths.iterator();
        Iterator<Path> dropoffPathIterator = plainDropoffPaths.iterator();

        for (PassengerRequest pickup : appendTask.pickupOrder) {
            Path plainPickupPath = pickupPathIterator.next();

            if (plainPickupPath != null) {
                VrpPathWithTravelData path = VrpPaths.createPath(currentLink, pickup.getFromLink(), currentTime, plainPickupPath, travelTime);
                paths.add(path);

                DrtDriveTask driveTask = new DrtDriveTask(path, DrtDriveTask.TYPE);
                driveTasks.add(driveTask);
                schedule.addTask(driveTask);

                currentTask = driveTask;
                currentLink = pickup.getFromLink();
                currentTime = path.getArrivalTime();
            }

            if (currentTask instanceof AmodeusStopTask) {
                ((AmodeusStopTask) currentTask).addPickupRequest(pickup);
                currentRequests.add(pickup);
            } else {
                AmodeusStopTask pickupTask = new AmodeusStopTask(currentTime, currentTime + timing.getMinimumPickupDurationPerStop(), pickup.getFromLink(), StopType.Pickup);
                pickupTask.addPickupRequest(pickup);

                schedule.addTask(pickupTask);
                currentTask = pickupTask;
                currentRequests.add(pickup);
                currentTime += timing.getMinimumPickupDurationPerStop();
            }
        }

        for (PassengerRequest dropoff : appendTask.dropoffOrder) {
            Path plainDropoffPath = dropoffPathIterator.next();

            if (plainDropoffPath != null) {
                VrpPathWithTravelData path = VrpPaths.createPath(currentLink, dropoff.getToLink(), currentTime, plainDropoffPath, travelTime);
                paths.add(path);

                DrtDriveTask driveTask = new DrtDriveTask(path, DrtDriveTask.TYPE);
                driveTasks.add(driveTask);
                schedule.addTask(driveTask);

                currentTask = driveTask;
                currentLink = dropoff.getToLink();
                currentTime = path.getArrivalTime();
            }

            if (currentTask instanceof AmodeusStopTask) {
                ((AmodeusStopTask) currentTask).addDropoffRequest(dropoff);
                currentRequests.remove(dropoff);
            } else {
                AmodeusStopTask dropoffTask = new AmodeusStopTask(currentTime, currentTime + timing.getMinimumDropoffDurationPerStop(), dropoff.getToLink(), StopType.Dropoff);
                dropoffTask.addDropoffRequest(dropoff);

                schedule.addTask(dropoffTask);
                currentTask = dropoffTask;
                currentRequests.remove(dropoff);
                currentTime += timing.getMinimumDropoffDurationPerStop();
            }
        }

        if (currentTask.getEndTime() < scheduleEndTime) {
            schedule.addTask(new DrtStayTask(currentTime, scheduleEndTime, currentLink));
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
