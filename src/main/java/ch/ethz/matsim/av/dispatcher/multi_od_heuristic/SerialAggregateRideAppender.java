package ch.ethz.matsim.av.dispatcher.multi_od_heuristic;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.path.VrpPath;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.matsim.av.config.operator.TimingConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.multi_od_heuristic.aggregation.AggregatedRequest;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;
import ch.ethz.matsim.av.schedule.AVTask;

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
        AVStayTask stayTask = (AVStayTask) Schedules.getLastTask(schedule);

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
        AVTask currentTask = stayTask;
        LinkedList<AVRequest> currentRequests = new LinkedList<>();

        LinkedList<VrpPathWithTravelData> paths = new LinkedList<>();
        LinkedList<AVDriveTask> driveTasks = new LinkedList<>();

        if (stayTask.getStatus() == Task.TaskStatus.STARTED) {
            stayTask.setEndTime(startTime);
        } else {
            schedule.removeLastTask();
        }

        for (AVRequest pickup : pickupOrder) {
            if (!pickup.getFromLink().equals(currentLink)) {
                VrpPathWithTravelData path = VrpPaths.calcAndCreatePath(currentLink, pickup.getFromLink(), currentTime, router, travelTime);
                paths.add(path);

                AVDriveTask driveTask = new AVDriveTask(path, currentRequests);
                driveTasks.add(driveTask);
                schedule.addTask(driveTask);

                currentTask = driveTask;
                currentLink = pickup.getFromLink();
                currentTime = path.getArrivalTime();
                //System.err.println("PickupDrive with arrival time: " + String.valueOf(currentTime));
            }

            if (currentTask instanceof AVPickupTask) {
                ((AVPickupTask) currentTask).addRequest(pickup);
                currentRequests.add(pickup);
                //System.err.println("Request added to pickup");
            } else {
                AVPickupTask pickupTask = new AVPickupTask(
                        currentTime,
                        currentTime + timing.getPickupDurationPerStop(),
                        pickup.getFromLink(), Double.NEGATIVE_INFINITY,
                        Arrays.asList(pickup)
                );

                schedule.addTask(pickupTask);
                currentTask = pickupTask;
                currentRequests.add(pickup);
                currentTime += timing.getPickupDurationPerStop();
                //System.err.println("Pickup with finish time: " + String.valueOf(currentTime));
            }
        }

        for (AVRequest dropoff : dropoffOrder) {
            if (!dropoff.getToLink().equals(currentLink)) {
                VrpPathWithTravelData path = VrpPaths.calcAndCreatePath(currentLink, dropoff.getToLink(), currentTime, router, travelTime);
                paths.add(path);

                AVDriveTask driveTask = new AVDriveTask(path, currentRequests);
                driveTasks.add(driveTask);
                schedule.addTask(driveTask);

                currentTask = driveTask;
                currentLink = dropoff.getToLink();
                currentTime = path.getArrivalTime();
                //System.err.println("DropoffDrive with arrival time: " + String.valueOf(currentTime));
            }

            if (currentTask instanceof AVDropoffTask) {
                ((AVDropoffTask) currentTask).addRequest(dropoff);
                currentRequests.remove(dropoff);
                //System.err.println("Request added to dropoff");
            } else {
                AVDropoffTask dropoffTask = new AVDropoffTask(
                        currentTime,
                        currentTime + timing.getDropoffDurationPerStop(),
                        dropoff.getToLink(),
                        Arrays.asList(dropoff)
                );

                schedule.addTask(dropoffTask);
                currentTask = dropoffTask;
                currentRequests.remove(dropoff);
                currentTime += timing.getDropoffDurationPerStop();
                //System.err.println("Dropoff with finish time: " + String.valueOf(currentTime));
            }
        }

        if (currentTask.getEndTime() < scheduleEndTime) {
            schedule.addTask(new AVStayTask(currentTime, scheduleEndTime, currentLink));
        }

        // Reconstruct travel distances
        for (AVRequest customerRequest : requests) {
            double distance = 0.0;

            for (AVDriveTask task : driveTasks) {
                if (task.getRequests().contains(customerRequest)) {
                    VrpPath path = task.getPath();

                    for (int i = 0; i < path.getLinkCount(); i++) {
                        distance += path.getLink(i).getLength();
                    }
                }
            }

            customerRequest.getRoute().setDistance(distance);
        }
    }

    @Override
    public void update() {

    }
}