/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Arrays;

import org.matsim.amodeus.dvrp.schedule.AmodeusDriveTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusDropoffTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusPickupTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/** for vehicles that are in stay task and should pickup a customer at the link:
 * 1) finish stay task 2) append pickup task 3) append drive task 4) append
 * dropoff task 5) append new stay task */
/* package */ final class AcceptRequestDirective extends FuturePathDirective {
    final RoboTaxi roboTaxi;
    final PassengerRequest avRequest;
    final double getTimeNow;
    final double dropoffDurationPerStop;

    public AcceptRequestDirective(RoboTaxi roboTaxi, PassengerRequest avRequest, //
            FuturePathContainer futurePathContainer, final double getTimeNow, double dropoffDurationPerStop) {
        super(futurePathContainer);
        this.roboTaxi = roboTaxi;
        this.avRequest = avRequest;
        this.getTimeNow = getTimeNow;
        this.dropoffDurationPerStop = dropoffDurationPerStop;
    }

    @Override
    void executeWithPath(final VrpPathWithTravelData vrpPathWithTravelData) {
        final Schedule schedule = roboTaxi.getSchedule();
        final AmodeusStayTask avStayTask = (AmodeusStayTask) Schedules.getLastTask(schedule);
        final double scheduleEndTime = avStayTask.getEndTime();
        GlobalAssert.that(scheduleEndTime == schedule.getEndTime());
        final double begDropoffTime = vrpPathWithTravelData.getArrivalTime();
        final double endDropoffTime = begDropoffTime + dropoffDurationPerStop;

        if (endDropoffTime < scheduleEndTime) {
            avStayTask.setEndTime(getTimeNow); // finish the last task now

            AmodeusPickupTask pickupTask = new AmodeusPickupTask( //
                    getTimeNow, // start of pickup
                    futurePathContainer.getStartTime(), // end of pickup
                    avRequest.getFromLink(), // location of driving start
                    0.0);
            pickupTask.addRequest(avRequest); // serving only one request at a time
            schedule.addTask(pickupTask);

            schedule.addTask(new AmodeusDriveTask( //
                    vrpPathWithTravelData, Arrays.asList(avRequest)));

            // final double endDropoffTime =
            // vrpPathWithTravelData.getArrivalTime() + dropoffDurationPerStop;
            AmodeusDropoffTask dropoffTask = new AmodeusDropoffTask( //
                    begDropoffTime, // start of dropoff
                    endDropoffTime, // end of dropoff
                    avRequest.getToLink() // location of dropoff
            );
            dropoffTask.addRequest(avRequest);
            schedule.addTask(dropoffTask);

            ScheduleUtils.makeWhole(roboTaxi, endDropoffTime, scheduleEndTime, avRequest.getToLink());
        } else
            reportExecutionBypass(endDropoffTime - scheduleEndTime);
    }

}
