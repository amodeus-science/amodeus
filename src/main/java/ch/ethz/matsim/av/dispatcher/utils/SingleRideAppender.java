package ch.ethz.matsim.av.dispatcher.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.matsim.av.config.operator.TimingConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import ch.ethz.refactoring.schedule.AmodeusDriveTask;
import ch.ethz.refactoring.schedule.AmodeusDropoffTask;
import ch.ethz.refactoring.schedule.AmodeusPickupTask;
import ch.ethz.refactoring.schedule.AmodeusStayTask;

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
        final public AVRequest request;
        final public AVVehicle vehicle;

        final public Future<Path> pickup;
        final public Future<Path> dropoff;

        final public double time;

        public AppendTask(AVRequest request, AVVehicle vehicle, double time, Future<Path> pickup, Future<Path> dropoff) {
            this.request = request;
            this.vehicle = vehicle;
            this.pickup = pickup;
            this.dropoff = dropoff;
            this.time = time;
        }
    }

    public void schedule(AVRequest request, AVVehicle vehicle, double now) {
        Schedule schedule = vehicle.getSchedule();
        AmodeusStayTask stayTask = (AmodeusStayTask) Schedules.getLastTask(schedule);

        Future<Path> pickup = router.calcLeastCostPath(stayTask.getLink().getToNode(), request.getFromLink().getFromNode(), now, null, null);
        Future<Path> dropoff = router.calcLeastCostPath(request.getFromLink().getToNode(), request.getToLink().getFromNode(), now, null, null);

        tasks.add(new AppendTask(request, vehicle, now, pickup, dropoff));
    }

    public void schedule(AppendTask task, Path plainPickupPath, Path plainDropoffPath) {
        AVRequest request = task.request;
        AVVehicle vehicle = task.vehicle;
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
        VrpPathWithTravelData dropoffPath = VrpPaths.createPath(request.getFromLink(), request.getToLink(), pickupPath.getArrivalTime() + timing.getPickupDurationPerStop(),
                plainDropoffPath, travelTime);

        AmodeusDriveTask pickupDriveTask = new AmodeusDriveTask(pickupPath);
        AmodeusPickupTask pickupTask = new AmodeusPickupTask(pickupPath.getArrivalTime(), pickupPath.getArrivalTime() + timing.getPickupDurationPerStop(), request.getFromLink(),
                Double.NEGATIVE_INFINITY, Arrays.asList(request));
        AmodeusDriveTask dropoffDriveTask = new AmodeusDriveTask(dropoffPath, Arrays.asList(request));
        AmodeusDropoffTask dropoffTask = new AmodeusDropoffTask(dropoffPath.getArrivalTime(), dropoffPath.getArrivalTime() + timing.getDropoffDurationPerStop(),
                request.getToLink(), Arrays.asList(request));

        if (stayTask.getStatus() == Task.TaskStatus.STARTED) {
            stayTask.setEndTime(startTime);
        } else {
            schedule.removeLastTask();
        }

        schedule.addTask(pickupDriveTask);
        schedule.addTask(pickupTask);
        schedule.addTask(dropoffDriveTask);
        schedule.addTask(dropoffTask);

        double distance = 0.0;
        for (int i = 0; i < dropoffPath.getLinkCount(); i++) {
            distance += dropoffPath.getLink(i).getLength();
        }
        request.getRoute().setDistance(distance);

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
