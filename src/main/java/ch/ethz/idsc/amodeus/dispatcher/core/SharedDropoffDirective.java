package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Arrays;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/*package*/ class SharedDropoffDirective extends SharedFixedLocationDirective {

    public SharedDropoffDirective(RoboTaxi robotaxi, AVRequest avRequest, double getTimeNow, double durationOfTask) {
        super(robotaxi, avRequest, getTimeNow, durationOfTask);
    }

    @Override
    public void execute() {
        final Schedule schedule = robotaxi.getSchedule();
        final AVStayTask avStayTask = (AVStayTask) Schedules.getLastTask(schedule);
        final double scheduleEndTime = avStayTask.getEndTime();
        GlobalAssert.that(scheduleEndTime == schedule.getEndTime());
        GlobalAssert.that(avStayTask.getLink().equals(avRequest.getToLink()));
        double endTimeDropoff = getTimeNow + durationOfTask;

        if (endTimeDropoff < scheduleEndTime) {

            avStayTask.setEndTime(getTimeNow); // finish the last task now

            schedule.addTask(new AVDropoffTask( //
                    getTimeNow, // start of dropoff
                    getTimeNow + durationOfTask, // end of dropoff
                    avRequest.getToLink(), // location of dropoff
                    Arrays.asList(avRequest)));

            Link destLink = avStayTask.getLink();
            ScheduleUtils.makeWhole(robotaxi, getTimeNow + durationOfTask, scheduleEndTime, destLink);

            // jan: following computation is mandatory for the internal scoring
            // function
            // final double distance = VrpPathUtils.getDistance(vrpPathWithTravelData);
            // nextRequest.getRoute().setDistance(distance);
        }
    }
}
