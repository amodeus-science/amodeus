package org.matsim.amodeus.components.dispatcher.multi_od_heuristic;

import java.util.LinkedList;
import java.util.Queue;

import org.matsim.amodeus.components.dispatcher.multi_od_heuristic.aggregation.AggregatedRequest;
import org.matsim.amodeus.config.modal.TimingConfig;
import org.matsim.amodeus.dvrp.schedule.AmodeusDriveTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask.StopType;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;

public class SerialAggregateRideAppender implements AggregateRideAppender {
    final private LeastCostPathCalculator router;
    final private TravelTime travelTime;
    final private TimingConfig timing;
    final private TravelTimeEstimator travelTimeEstimator;

    public SerialAggregateRideAppender(TimingConfig timing, LeastCostPathCalculator router, TravelTime travelTime, TravelTimeEstimator travelTimeEstimator) {
        this.router = router;
        this.travelTime = travelTime;
        this.timing = timing;
        this.travelTimeEstimator = travelTimeEstimator;
    }

    public void schedule(AggregatedRequest request, DvrpVehicle vehicle, double now) {
        Schedule schedule = vehicle.getSchedule();
        AmodeusStayTask stayTask = (AmodeusStayTask) Schedules.getLastTask(schedule);

        double startTime = 0.0;
        double scheduleEndTime = schedule.getEndTime();

        if (stayTask.getStatus() == Task.TaskStatus.STARTED) {
            startTime = now;
        } else {
            startTime = stayTask.getBeginTime();
        }

        LinkedList<PassengerRequest> requests = new LinkedList<>();
        LinkedList<PassengerRequest> pickups = new LinkedList<>();
        LinkedList<PassengerRequest> dropoffs = new LinkedList<>();

        requests.addAll(request.getSlaveRequests());
        requests.add(request.getMasterRequest());
        pickups.addAll(request.getSlaveRequests());
        pickups.add(request.getMasterRequest());
        dropoffs.addAll(request.getSlaveRequests());
        dropoffs.add(request.getMasterRequest());

        Queue<PassengerRequest> pickupOrder = new LinkedList<>();
        Queue<PassengerRequest> dropoffOrder = new LinkedList<>();

        Link current = stayTask.getLink();

        while (pickups.size() > 0) {
            PassengerRequest closestRequest = null;
            double shortestDistance = Double.POSITIVE_INFINITY;

            for (PassengerRequest pickup : pickups) {
                double distance = travelTimeEstimator.estimateTravelTime(current, pickup.getFromLink(), startTime);

                if (distance < shortestDistance) {
                    closestRequest = pickup;
                    shortestDistance = distance;
                }
            }

            pickupOrder.add(closestRequest);
            pickups.remove(closestRequest);
            current = closestRequest.getFromLink();
        }

        while (dropoffs.size() > 0) {
            PassengerRequest closestRequest = null;
            double shortestDistance = Double.POSITIVE_INFINITY;

            for (PassengerRequest dropoff : dropoffs) {
                double distance = travelTimeEstimator.estimateTravelTime(current, dropoff.getToLink(), startTime);

                if (distance < shortestDistance) {
                    closestRequest = dropoff;
                    shortestDistance = distance;
                }
            }

            dropoffOrder.add(closestRequest);
            dropoffs.remove(closestRequest);
            current = closestRequest.getToLink();
        }

        Link currentLink = stayTask.getLink();
        double currentTime = startTime;
        Task currentTask = stayTask;
        LinkedList<PassengerRequest> currentRequests = new LinkedList<>();

        LinkedList<VrpPathWithTravelData> paths = new LinkedList<>();
        LinkedList<AmodeusDriveTask> driveTasks = new LinkedList<>();

        if (stayTask.getStatus() == Task.TaskStatus.STARTED) {
            stayTask.setEndTime(startTime);
        } else {
            schedule.removeLastTask();
        }

        for (PassengerRequest pickup : pickupOrder) {
            if (!pickup.getFromLink().equals(currentLink)) {
                VrpPathWithTravelData path = VrpPaths.calcAndCreatePath(currentLink, pickup.getFromLink(), currentTime, router, travelTime);
                paths.add(path);

                AmodeusDriveTask driveTask = new AmodeusDriveTask(path, currentRequests);
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

        for (PassengerRequest dropoff : dropoffOrder) {
            if (!dropoff.getToLink().equals(currentLink)) {
                VrpPathWithTravelData path = VrpPaths.calcAndCreatePath(currentLink, dropoff.getToLink(), currentTime, router, travelTime);
                paths.add(path);

                AmodeusDriveTask driveTask = new AmodeusDriveTask(path, currentRequests);
                driveTasks.add(driveTask);
                schedule.addTask(driveTask);

                currentTask = driveTask;
                currentLink = dropoff.getToLink();
                currentTime = path.getArrivalTime();
            }

            if (currentTask instanceof AmodeusStopTask) {
                ((AmodeusStopTask) currentTask).addDropoffRequest(dropoff);
                currentRequests.remove(dropoff);
                // System.err.println("Request added to dropoff");
            } else {
                AmodeusStopTask dropoffTask = new AmodeusStopTask(currentTime, currentTime + timing.getMinimumDropoffDurationPerStop(), dropoff.getToLink(), StopType.Dropoff);
                dropoffTask.addDropoffRequest(dropoff);

                schedule.addTask(dropoffTask);
                currentTask = dropoffTask;
                currentRequests.remove(dropoff);
                currentTime += timing.getMinimumDropoffDurationPerStop();
                // System.err.println("Dropoff with finish time: " + String.valueOf(currentTime));
            }
        }

        if (currentTask.getEndTime() < scheduleEndTime) {
            schedule.addTask(new AmodeusStayTask(currentTime, scheduleEndTime, currentLink));
        }
    }

    @Override
    public void update() {

    }
}