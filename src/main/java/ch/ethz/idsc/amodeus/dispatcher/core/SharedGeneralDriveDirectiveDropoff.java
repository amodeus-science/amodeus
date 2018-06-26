/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Arrays;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedAVMealType;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/**
 * for vehicles that are in stay task and should pickup a customer at the link:
 * 1) finish stay task 2) append pickup task 3) append drive task 4) append
 * dropoff task 5) append new stay task
 */
/* package */ final class SharedGeneralDriveDirectiveDropoff extends FuturePathDirective {
	final SharedRoboTaxi robotaxi;
	final AVRequest nextRequest;
	final double getTimeNow;
	final double dropoffDurationPerStop;
	final double pickupDurationPerStop;
	final SharedAVMealType nextMealType;

	public SharedGeneralDriveDirectiveDropoff(SharedRoboTaxi robotaxi, AVRequest nextRequest, //
			FuturePathContainer futurePathContainer, final double getTimeNow, double dropoffDurationPerStop,
			double pickupDurationPerStop, SharedAVMealType nextMealType) {
		super(futurePathContainer);
		this.robotaxi = robotaxi;
		this.nextRequest = nextRequest;
		this.getTimeNow = getTimeNow;
		this.dropoffDurationPerStop = dropoffDurationPerStop;
		this.pickupDurationPerStop = pickupDurationPerStop;
		this.nextMealType = nextMealType;
	}

	@Override
	void executeWithPath(final VrpPathWithTravelData vrpPathWithTravelData) {
		final Schedule schedule = robotaxi.getSchedule();
		final AVStayTask avStayTask = (AVStayTask) Schedules.getLastTask(schedule);
		final double scheduleEndTime = avStayTask.getEndTime();
		GlobalAssert.that(scheduleEndTime == schedule.getEndTime());
		final double starTimeNextMeal = vrpPathWithTravelData.getArrivalTime();
		final double endTimeNextMeal = starTimeNextMeal + dropoffDurationPerStop;

		if (endTimeNextMeal < scheduleEndTime) {

			if (nextRequest != null) {
				GlobalAssert.that(nextMealType != null);
				avStayTask.setEndTime(getTimeNow); // finish the last task now

				schedule.addTask(new AVDriveTask( //
						vrpPathWithTravelData, Arrays.asList(nextRequest)));

				// final double endDropoffTime = vrpPathWithTravelData.getArrivalTime() +
				// dropoffDurationPerStop;
				Link destLink = null;
				if (nextMealType.equals(SharedAVMealType.PICKUP)) {
					destLink = nextRequest.getFromLink();
					schedule.addTask(new AVPickupTask( //
							starTimeNextMeal, // start of dropoff
							endTimeNextMeal, destLink, // location of dropoff
							Arrays.asList(nextRequest)));
				} else if (nextMealType.equals(SharedAVMealType.DROPOFF)) {
					destLink = nextRequest.getToLink();

					schedule.addTask(new AVDropoffTask( //
							starTimeNextMeal, // start of dropoff
							endTimeNextMeal, destLink, // location of dropoff
							Arrays.asList(nextRequest)));
				} else {
					throw new IllegalArgumentException("Unknown SharedAVMealType -- please specify it !!!--");
				}
				GlobalAssert.that(destLink != null);
				ScheduleUtils.makeWhole(robotaxi, endTimeNextMeal, scheduleEndTime, destLink);

				// jan: following computation is mandatory for the internal scoring
				// function
				final double distance = VrpPathUtils.getDistance(vrpPathWithTravelData);
				nextRequest.getRoute().setDistance(distance);
			}
			// Else do nothing as you alredy have a stay task...
		} else
			reportExecutionBypass(endTimeNextMeal - scheduleEndTime);
	}

}
