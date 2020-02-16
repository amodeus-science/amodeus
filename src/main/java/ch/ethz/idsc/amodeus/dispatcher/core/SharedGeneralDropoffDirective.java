/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Objects;
import java.util.Optional;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.schedule.Task.TaskStatus;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/** for vehicles that are in stay task and should dropoff a customer at the link:
 * 1) finish stay task 2) append dropoff task 3) if more customers planned append drive task
 * 4) append new stay task */
/* package */ final class SharedGeneralDropoffDirective extends FuturePathDirective {
    final RoboTaxi roboTaxi;
    final AVRequest currentRequest;
    final double getTimeNow;
    final double dropoffDurationPerStop;

    public SharedGeneralDropoffDirective(RoboTaxi roboTaxi, AVRequest currentRequest, //
            FuturePathContainer futurePathContainer, final double getTimeNow, double dropoffDurationPerStop) {
        super(futurePathContainer);
        this.roboTaxi = roboTaxi;
        this.currentRequest = currentRequest;
        this.getTimeNow = getTimeNow;
        this.dropoffDurationPerStop = dropoffDurationPerStop;
    }

    private Optional<AVDropoffTask> getPreviousDropoffTask(Schedule schedule, Link link) {
        int candidateIndex = schedule.getTaskCount() - 1 - 2;

        if (candidateIndex > 0) {
            Task task = schedule.getTasks().get(candidateIndex);

            if (task.getStatus().equals(TaskStatus.PLANNED)) {
                if (task instanceof AVDropoffTask) {
                    if (((AVDropoffTask) task).getLink() == link) {
                        return Optional.of((AVDropoffTask) task);
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
        final boolean moreRequestsToServe = Objects.nonNull(vrpPathWithTravelData);
        final double endTimeNextTask = (moreRequestsToServe) ? vrpPathWithTravelData.getArrivalTime() : getTimeNow + dropoffDurationPerStop;
        GlobalAssert.that(avStayTask.getLink().equals(currentRequest.getToLink()));

        if (endTimeNextTask < scheduleEndTime) {
            Optional<AVDropoffTask> previousDropoffTask = getPreviousDropoffTask(schedule, currentRequest.getToLink());

            if (!previousDropoffTask.isPresent()) { // No previous task available at that place
                avStayTask.setEndTime(getTimeNow); // finish the last task now

                AVDropoffTask dropoffTask = new AVDropoffTask( //
                        getTimeNow, // start of dropoff
                        getTimeNow + dropoffDurationPerStop, // end of dropoff
                        currentRequest.getToLink() // location of dropoff
                );

                dropoffTask.addRequest(currentRequest);
                schedule.addTask(dropoffTask);

                Link destLink = avStayTask.getLink();
                ScheduleUtils.makeWhole(roboTaxi, getTimeNow + dropoffDurationPerStop, scheduleEndTime, destLink);

                // jan: following computation is mandatory for the internal scoring
                // function
                // final double distance = VrpPathUtils.getDistance(vrpPathWithTravelData);
                // nextRequest.getRoute().setDistance(distance);
            } else { // There is a previous task available at that place
                previousDropoffTask.get().addRequest(currentRequest);
            }
        } else
            reportExecutionBypass(endTimeNextTask - scheduleEndTime);
    }
}
