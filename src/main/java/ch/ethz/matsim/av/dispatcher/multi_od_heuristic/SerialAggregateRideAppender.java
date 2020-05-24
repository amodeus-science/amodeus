package ch.ethz.matsim.av.dispatcher.multi_od_heuristic;

import java.util.LinkedList;
import java.util.Queue;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.matsim.av.config.modal.TimingConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.multi_od_heuristic.aggregation.AggregatedRequest;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.refactoring.schedule.AmodeusDriveTask;
import ch.ethz.refactoring.schedule.AmodeusDropoffTask;
import ch.ethz.refactoring.schedule.AmodeusPickupTask;
import ch.ethz.refactoring.schedule.AmodeusStayTask;

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

    public void schedule(AggregatedRequest request, AVVehicle vehicle, double now) {
        Schedule schedule = vehicle.getSchedule();
        AmodeusStayTask stayTask = (AmodeusStayTask) Schedules.getLastTask(schedule);

        double startTime = 0.0;
        double scheduleEndTime = schedule.getEndTime();

        if (stayTask.getStatus() == Task.TaskStatus.STARTED) {
            startTime = now;
        } else {
            startTime = stayTask.getBeginTime();
        }

        LinkedList<AVRequest> requests = new LinkedList<>();
        LinkedList<AVRequest> pickups = new LinkedList<>();
        LinkedList<AVRequest> dropoffs = new LinkedList<>();

        requests.addAll(request.getSlaveRequests());
        requests.add(request.getMasterRequest());
        pickups.addAll(request.getSlaveRequests());
        pickups.add(request.getMasterRequest());
        dropoffs.addAll(request.getSlaveRequests());
        dropoffs.add(request.getMasterRequest());

        Queue<AVRequest> pickupOrder = new LinkedList<>();
        Queue<AVRequest> dropoffOrder = new LinkedList<>();

        Link current = stayTask.getLink();

        while (pickups.size() > 0) {
            AVRequest closestRequest = null;
            double shortestDistance = Double.POSITIVE_INFINITY;

            for (AVRequest pickup : pickups) {
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
            AVRequest closestRequest = null;
            double shortestDistance = Double.POSITIVE_INFINITY;

            for (AVRequest dropoff : dropoffs) {
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
        LinkedList<AVRequest> currentRequests = new LinkedList<>();

        LinkedList<VrpPathWithTravelData> paths = new LinkedList<>();
        LinkedList<AmodeusDriveTask> driveTasks = new LinkedList<>();

        if (stayTask.getStatus() == Task.TaskStatus.STARTED) {
            stayTask.setEndTime(startTime);
        } else {
            schedule.removeLastTask();
        }

        for (AVRequest customerRequest : pickupOrder) {
            // REFACTOR: We are reconstructing the distances here, while there should already be a planned distance in the route depending on whether we predict it or not.
            // If we don't predict a distance, the scoring part should be able to cope with that. Certainly, summing up the *actual* driven distance as here, we can also do
            // (more easily) from the events. What we're interested in actually is the planned distance, which should be passed by the AVTransit event anyways. Need to find
            // a mechanism for that. Because this will not work anyways with Amodeus dispatchers!
            customerRequest.getRoute().setDistance(0.0);
        }

        for (AVRequest pickup : pickupOrder) {
            if (!pickup.getFromLink().equals(currentLink)) {
                VrpPathWithTravelData path = VrpPaths.calcAndCreatePath(currentLink, pickup.getFromLink(), currentTime, router, travelTime);
                paths.add(path);

                AmodeusDriveTask driveTask = new AmodeusDriveTask(path, currentRequests);
                driveTasks.add(driveTask);
                schedule.addTask(driveTask);

                currentTask = driveTask;
                currentLink = pickup.getFromLink();
                currentTime = path.getArrivalTime();
                // System.err.println("PickupDrive with arrival time: " + String.valueOf(currentTime));

                double driveDistance = VrpPaths.calcDistance(path);

                for (AVRequest customerRequest : currentRequests) {
                    // REFACTOR
                    customerRequest.getRoute().setDistance(customerRequest.getRoute().getDistance() + driveDistance);
                }
            }

            if (currentTask instanceof AmodeusPickupTask) {
                ((AmodeusPickupTask) currentTask).addRequest(pickup);
                currentRequests.add(pickup);
                // System.err.println("Request added to pickup");
            } else {
                AmodeusPickupTask pickupTask = new AmodeusPickupTask(currentTime, currentTime + timing.getMinimumPickupDurationPerStop(), pickup.getFromLink(), Double.NEGATIVE_INFINITY);
                pickupTask.addRequest(pickup);

                schedule.addTask(pickupTask);
                currentTask = pickupTask;
                currentRequests.add(pickup);
                currentTime += timing.getMinimumPickupDurationPerStop();
                // System.err.println("Pickup with finish time: " + String.valueOf(currentTime));
            }
        }

        for (AVRequest dropoff : dropoffOrder) {
            if (!dropoff.getToLink().equals(currentLink)) {
                VrpPathWithTravelData path = VrpPaths.calcAndCreatePath(currentLink, dropoff.getToLink(), currentTime, router, travelTime);
                paths.add(path);

                AmodeusDriveTask driveTask = new AmodeusDriveTask(path, currentRequests);
                driveTasks.add(driveTask);
                schedule.addTask(driveTask);

                currentTask = driveTask;
                currentLink = dropoff.getToLink();
                currentTime = path.getArrivalTime();
                // System.err.println("DropoffDrive with arrival time: " + String.valueOf(currentTime));

                double driveDistance = VrpPaths.calcDistance(path);

                for (AVRequest customerRequest : currentRequests) {
                    // REFACTOR
                    customerRequest.getRoute().setDistance(customerRequest.getRoute().getDistance() + driveDistance);
                }
            }

            if (currentTask instanceof AmodeusDropoffTask) {
                ((AmodeusDropoffTask) currentTask).addRequest(dropoff);
                currentRequests.remove(dropoff);
                // System.err.println("Request added to dropoff");
            } else {
                AmodeusDropoffTask dropoffTask = new AmodeusDropoffTask(currentTime, currentTime + timing.getMinimumDropoffDurationPerStop(), dropoff.getToLink());
                dropoffTask.addRequest(dropoff);

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