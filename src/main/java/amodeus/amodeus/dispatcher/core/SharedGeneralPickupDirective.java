/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.List;

import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask.StopType;
import org.matsim.contrib.drt.schedule.DrtStayTask;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;

import amodeus.amodeus.util.math.GlobalAssert;

/** for vehicles that are in stay task and should pickup a customer at the link:
 * 1) finish stay task 2) append pickup task 3) append drive task 4) append new stay task */
/* package */ final class SharedGeneralPickupDirective implements DirectiveInterface {
    final RoboTaxi roboTaxi;
    final List<PassengerRequest> sameOriginRequests;
    final double getTimeNow;
    final private double pickupDurationPerStop;

    public SharedGeneralPickupDirective(RoboTaxi roboTaxi, List<PassengerRequest> sameOriginRequests, //
            double pickupDurationPerStop, final double getTimeNow) {
        this.roboTaxi = roboTaxi;
        this.sameOriginRequests = sameOriginRequests;
        this.getTimeNow = getTimeNow;
        this.pickupDurationPerStop = pickupDurationPerStop;

        // all requests must have same from link
        GlobalAssert.that(sameOriginRequests.stream().map(PassengerRequest::getFromLink).distinct().count() == 1);
    }

    @Override
    public void execute() {
        final Schedule schedule = roboTaxi.getSchedule();
        final DrtStayTask avStayTask = (DrtStayTask) Schedules.getLastTask(schedule);
        final double scheduleEndTime = avStayTask.getEndTime();
        GlobalAssert.that(scheduleEndTime == schedule.getEndTime());
        
        boolean isLast = schedule.getCurrentTask() == Schedules.getLastTask(schedule);
        
        double startTime = getTimeNow;
        
        if (isLast) {
            schedule.getCurrentTask().setEndTime(getTimeNow);
        } else {
            schedule.removeLastTask();
            startTime = Schedules.getLastTask(schedule).getEndTime();
        }
        
        double endTaskTime = pickupDurationPerStop + startTime;

        if (endTaskTime < scheduleEndTime) {

            AmodeusStopTask pickupTask = new AmodeusStopTask( //
                    startTime, // start of pickup
                    endTaskTime, // end of pickup
                    sameOriginRequests.get(0).getFromLink(), // location of driving start
                    StopType.Pickup
                    );
            sameOriginRequests.forEach(pickupTask::addPickupRequest);
            schedule.addTask(pickupTask);

            GlobalAssert.that(endTaskTime < scheduleEndTime);
            ScheduleUtils.makeWhole(roboTaxi, endTaskTime, scheduleEndTime, //
                    sameOriginRequests.get(0).getFromLink());

        } else
            throw new IllegalStateException();
    }

}
