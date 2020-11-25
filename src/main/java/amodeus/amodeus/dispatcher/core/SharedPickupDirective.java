/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask.StopType;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;

import amodeus.amodeus.util.math.GlobalAssert;

/** for vehicles that are in dropoff or pickup task and new request is assigned.
 * 1) finish pickup or dropoff task 2) append drive task 3) append new stay task */
/* package */ final class SharedPickupDirective extends SharedFixedLocationDirective {

    public SharedPickupDirective(RoboTaxi roboTaxi, PassengerRequest avRequest, double getTimeNow, double durationOfTask) {
        super(roboTaxi, avRequest, getTimeNow, durationOfTask);
    }

    @Override
    public void execute() {
        final Schedule schedule = roboTaxi.getSchedule();
        final AmodeusStayTask avStayTask = (AmodeusStayTask) Schedules.getLastTask(schedule);
        final double scheduleEndTime = avStayTask.getEndTime();
        final double endTaskTime = getTimeNow + durationOfTask;
        GlobalAssert.that(scheduleEndTime == schedule.getEndTime());

        if (endTaskTime < scheduleEndTime) {
            avStayTask.setEndTime(getTimeNow); // finish the last task now

            AmodeusStopTask pickupTask = new AmodeusStopTask( //
                    getTimeNow, // start of pickup
                    endTaskTime, // end of pickup
                    avRequest.getFromLink(), // location of driving start
                    StopType.Pickup
                    );
            pickupTask.addPickupRequest(avRequest); // serving only one request at a time
            schedule.addTask(pickupTask);

            ScheduleUtils.makeWhole(roboTaxi, endTaskTime, scheduleEndTime, avRequest.getFromLink());

            // jan: following computation is mandatory for the internal scoring
            // function
            // final double distance = VrpPathUtils.getDistance(vrpPathWithTravelData);
            // nextRequest.getRoute().setDistance(distance);
        }
    }

}
