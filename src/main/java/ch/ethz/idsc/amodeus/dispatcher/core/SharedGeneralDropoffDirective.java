/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Objects;

import org.matsim.amodeus.dvrp.request.AVRequest;
import org.matsim.amodeus.dvrp.schedule.AmodeusDropoffTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

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

    @Override
    void executeWithPath(final VrpPathWithTravelData vrpPathWithTravelData) {
        final Schedule schedule = roboTaxi.getSchedule();
        final AmodeusStayTask avStayTask = (AmodeusStayTask) Schedules.getLastTask(schedule);
        final double scheduleEndTime = avStayTask.getEndTime();
        GlobalAssert.that(scheduleEndTime == schedule.getEndTime());
        final boolean moreRequestsToServe = Objects.nonNull(vrpPathWithTravelData);
        final double endTimeNextTask = (moreRequestsToServe) ? vrpPathWithTravelData.getArrivalTime() : getTimeNow + dropoffDurationPerStop;
        GlobalAssert.that(avStayTask.getLink().equals(currentRequest.getToLink()));

        if (endTimeNextTask < scheduleEndTime) {
            avStayTask.setEndTime(getTimeNow); // finish the last task now

            AmodeusDropoffTask dropoffTaks = new AmodeusDropoffTask( //
                    getTimeNow, // start of dropoff
                    getTimeNow + dropoffDurationPerStop, // end of dropoff
                    currentRequest.getToLink() // location of dropoff
            );
            dropoffTaks.addRequest(currentRequest);
            schedule.addTask(dropoffTaks);

            Link destLink = avStayTask.getLink();
            ScheduleUtils.makeWhole(roboTaxi, getTimeNow + dropoffDurationPerStop, scheduleEndTime, destLink);

            // jan: following computation is mandatory for the internal scoring
            // function
            // final double distance = VrpPathUtils.getDistance(vrpPathWithTravelData);
            // nextRequest.getRoute().setDistance(distance);
        } else
            reportExecutionBypass(endTimeNextTask - scheduleEndTime);
    }
}
