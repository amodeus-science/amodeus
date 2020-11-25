package org.matsim.amodeus.components.dispatcher.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.matsim.amodeus.config.modal.TimingConfig;
import org.matsim.amodeus.dvrp.schedule.AmodeusDriveTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask.StopType;
import org.matsim.amodeus.plpc.ParallelLeastCostPathCalculator;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelTime;

public class SingleRideAppender {
    final private ParallelLeastCostPathCalculator router;
    final private TimingConfig timing;
    final private TravelTime travelTime;

    private List<AppendTask> tasks = new LinkedList<>();

    public SingleRideAppender(TimingConfig timing, ParallelLeastCostPathCalculator router, TravelTime travelTime) {
        this.router = router;
        this.timing = timing;
        this.travelTime = travelTime;
    }

    private class AppendTask {
        final public PassengerRequest request;
        final public DvrpVehicle vehicle;

        final public Future<Path> pickup;
        final public Future<Path> dropoff;

        final public double time;

        public AppendTask(PassengerRequest request, DvrpVehicle vehicle, double time, Future<Path> pickup, Future<Path> dropoff) {
            this.request = request;
            this.vehicle = vehicle;
            this.pickup = pickup;
            this.dropoff = dropoff;
            this.time = time;
        }
    }

    public void schedule(PassengerRequest request, DvrpVehicle vehicle, double now) {
        Schedule schedule = vehicle.getSchedule();
        AmodeusStayTask stayTask = (AmodeusStayTask) Schedules.getLastTask(schedule);

        Future<Path> pickup = router.calcLeastCostPath(stayTask.getLink().getToNode(), request.getFromLink().getFromNode(), now, null, null);
        Future<Path> dropoff = router.calcLeastCostPath(request.getFromLink().getToNode(), request.getToLink().getFromNode(), now, null, null);

        tasks.add(new AppendTask(request, vehicle, now, pickup, dropoff));
    }

    public void schedule(AppendTask task, Path plainPickupPath, Path plainDropoffPath) {
        PassengerRequest request = task.request;
        DvrpVehicle vehicle = task.vehicle;
        double now = task.time;

        Schedule schedule = vehicle.getSchedule();
        AmodeusStayTask stayTask = (AmodeusStayTask) Schedules.getLastTask(schedule);

        double startTime = 0.0;
        double scheduleEndTime = schedule.getEndTime();

        if (stayTask.getStatus() == Task.TaskStatus.STARTED) {
            startTime = now;
        } else {
            startTime = stayTask.getBeginTime();
        }

        VrpPathWithTravelData pickupPath = VrpPaths.createPath(stayTask.getLink(), request.getFromLink(), startTime, plainPickupPath, travelTime);
        VrpPathWithTravelData dropoffPath = VrpPaths.createPath(request.getFromLink(), request.getToLink(), pickupPath.getArrivalTime() + timing.getMinimumPickupDurationPerStop(),
                plainDropoffPath, travelTime);

        AmodeusDriveTask pickupDriveTask = new AmodeusDriveTask(pickupPath);
        AmodeusStopTask pickupTask = new AmodeusStopTask(pickupPath.getArrivalTime(), pickupPath.getArrivalTime() + timing.getMinimumPickupDurationPerStop(), request.getFromLink(),
                StopType.Pickup);
        pickupTask.addPickupRequest(request);
        AmodeusDriveTask dropoffDriveTask = new AmodeusDriveTask(dropoffPath, Arrays.asList(request));
        AmodeusStopTask dropoffTask = new AmodeusStopTask(dropoffPath.getArrivalTime(), dropoffPath.getArrivalTime() + timing.getMinimumDropoffDurationPerStop(),
                request.getToLink(), StopType.Dropoff);
        dropoffTask.addDropoffRequest(request);

        if (stayTask.getStatus() == Task.TaskStatus.STARTED) {
            stayTask.setEndTime(startTime);
        } else {
            schedule.removeLastTask();
        }

        schedule.addTask(pickupDriveTask);
        schedule.addTask(pickupTask);
        schedule.addTask(dropoffDriveTask);
        schedule.addTask(dropoffTask);

        if (dropoffTask.getEndTime() < scheduleEndTime) {
            schedule.addTask(new AmodeusStayTask(dropoffTask.getEndTime(), scheduleEndTime, dropoffTask.getLink()));
        }
    }

    public void update() {
        try {
            for (AppendTask task : tasks) {
                schedule(task, task.pickup.get(), task.dropoff.get());
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        tasks.clear();
    }
}
