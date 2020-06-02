/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import org.matsim.amodeus.dvrp.schedule.AmodeusDropoffTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;

import amodeus.amodeus.util.math.GlobalAssert;

/*package*/ class SharedDropoffDirective extends SharedFixedLocationDirective {

    public SharedDropoffDirective(RoboTaxi roboTaxi, PassengerRequest avRequest, double getTimeNow, double durationOfTask) {
        super(roboTaxi, avRequest, getTimeNow, durationOfTask);
    }

    @Override
    public void execute() {
        final Schedule schedule = roboTaxi.getSchedule();
        final AmodeusStayTask avStayTask = (AmodeusStayTask) Schedules.getLastTask(schedule);
        final double scheduleEndTime = avStayTask.getEndTime();
        GlobalAssert.that(scheduleEndTime == schedule.getEndTime());
        GlobalAssert.that(avStayTask.getLink().equals(avRequest.getToLink()));
        double endTimeDropoff = getTimeNow + durationOfTask;

        if (endTimeDropoff < scheduleEndTime) {
            avStayTask.setEndTime(getTimeNow); // finish the last task now

            AmodeusDropoffTask dropoffTask = new AmodeusDropoffTask( //
                    getTimeNow, // start of dropoff
                    getTimeNow + durationOfTask, // end of dropoff
                    avRequest.getToLink() // location of dropoff
            );
            dropoffTask.addRequest(avRequest);
            schedule.addTask(dropoffTask);

            Link destLink = avStayTask.getLink();
            ScheduleUtils.makeWhole(roboTaxi, getTimeNow + durationOfTask, scheduleEndTime, destLink);

            // jan: following computation is mandatory for the internal scoring
            // function
            // final double distance = VrpPathUtils.getDistance(vrpPathWithTravelData);
            // nextRequest.getRoute().setDistance(distance);
        }
    }
}
