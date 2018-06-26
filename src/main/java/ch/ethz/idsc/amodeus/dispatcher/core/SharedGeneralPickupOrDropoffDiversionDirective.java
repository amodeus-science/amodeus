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
/* package */ final class SharedGeneralPickupOrDropoffDiversionDirective extends FuturePathDirective {
	final SharedRoboTaxi robotaxi;
	final AVRequest nextRequest;
	final double getTimeNow;
	final double dropoffDurationPerStop;
	final double pickupDurationPerStop;
	final SharedAVMealType nextMealType;

	public SharedGeneralPickupOrDropoffDiversionDirective(SharedRoboTaxi robotaxi, AVRequest nextRequest, //
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

		final double endTimeNextMeal = nextMealType.equals(SharedAVMealType.DROPOFF)
				? starTimeNextMeal + dropoffDurationPerStop
				: starTimeNextMeal + pickupDurationPerStop;

		if (endTimeNextMeal < scheduleEndTime) {

			// Remove all pending tasks in the future
			while (Schedules.getLastTask(schedule).getEndTime() != schedule.getCurrentTask().getEndTime()) {
				schedule.removeLastTask();
			}

			// Add new drive task
			schedule.addTask(new AVDriveTask( //
					vrpPathWithTravelData, Arrays.asList(nextRequest)));

			// Add pickup or dropoff depending on meal type
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

		} else
			reportExecutionBypass(endTimeNextMeal - scheduleEndTime);
	}

}
