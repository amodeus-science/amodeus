/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Optional;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.schedule.Task.TaskStatus;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/** for vehicles that are in stay task and should pickup a customer at the link:
 * 1) finish stay task 2) append pickup task 3) append drive task 4) append new stay task */
/* package */ final class SharedGeneralPickupDirective extends FuturePathDirective {
    final RoboTaxi roboTaxi;
    final AVRequest currentRequest;
    final double getTimeNow;

    public SharedGeneralPickupDirective(RoboTaxi roboTaxi, AVRequest currentRequest, //
            FuturePathContainer futurePathContainer, final double getTimeNow) {
        super(futurePathContainer);
        this.roboTaxi = roboTaxi;
        this.currentRequest = currentRequest;
        this.getTimeNow = getTimeNow;
    }

    private Optional<AVPickupTask> getPreviousPickupTask(Schedule schedule, Link link) {
        int candidateIndex = schedule.getTaskCount() - 1 - 2;

        if (candidateIndex > 0) {
            Task task = schedule.getTasks().get(candidateIndex);

            if (task.getStatus().equals(TaskStatus.PLANNED)) {
                if (task instanceof AVPickupTask) {
                    if (((AVPickupTask) task).getLink() == link) {
                        return Optional.of((AVPickupTask) task);
                    }
                }
            }
        }

        return Optional.empty();
    }

    @Override
    void executeWithPath(final VrpPathWithTravelData vrpPathWithTravelData) {
        final Schedule schedule = roboTaxi.getSchedule();
        final AVStayTask avStayTask = (AVStayTask) Schedules.getLastTask(schedule);
        final double scheduleEndTime = avStayTask.getEndTime();
        GlobalAssert.that(scheduleEndTime == schedule.getEndTime());
        final double endTaskTime = vrpPathWithTravelData.getArrivalTime();

        if (endTaskTime < scheduleEndTime) {
            Optional<AVPickupTask> previousPickupTask = getPreviousPickupTask(schedule, currentRequest.getFromLink());

            if (!previousPickupTask.isPresent()) { // There is no pickup task available yet
                avStayTask.setEndTime(getTimeNow); // finish the last task now

                AVPickupTask pickupTask = new AVPickupTask( //
                        getTimeNow, // start of pickup
                        futurePathContainer.getStartTime(), // end of pickup
                        currentRequest.getFromLink(), // location of driving start
                        0.0);
                pickupTask.addRequest(currentRequest); // serving only one request at a time
                schedule.addTask(pickupTask);

                // schedule.addTask(new AVDriveTask( //
                // vrpPathWithTravelData, Arrays.asList(currentRequest)));
                // ScheduleUtils.makeWhole(robotaxi, endTaskTime, scheduleEndTime, vrpPathWithTravelData.getToLink());

                GlobalAssert.that(futurePathContainer.getStartTime() < scheduleEndTime);
                ScheduleUtils.makeWhole(roboTaxi, futurePathContainer.getStartTime(), scheduleEndTime, currentRequest.getFromLink());

                // jan: following computation is mandatory for the internal scoring
                // // function
                final double distance = VrpPathUtils.getDistance(vrpPathWithTravelData);
                currentRequest.getRoute().setDistance(distance);
            } else { // There is an old pickup task, so we can append the request there
                previousPickupTask.get().addRequest(currentRequest);
            }
        } else
            reportExecutionBypass(endTaskTime - scheduleEndTime);
    }

}
