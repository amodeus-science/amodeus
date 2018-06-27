/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Arrays;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.tracker.OnlineDriveTaskTracker;
import org.matsim.contrib.dvrp.tracker.TaskTracker;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedAVMealType;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusDriveTaskTracker;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.schedule.AVDriveTask;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;

/**
 * for vehicles that are currently driving, but should go to a new destination:
 * 1) change path of current drive task 2) remove former stay task with old
 * destination 3) append new stay task
 */
/* package */ final class SharedGeneralStayDirective extends VehicleDiversionDirective {
	final SharedRoboTaxi robotaxi;
	final AVRequest nextRequest;
	final double getTimeNow;
	final double dropoffDurationPerStop;
	final double pickupDurationPerStop;
	final SharedAVMealType nextMealType;

	SharedGeneralStayDirective(SharedRoboTaxi robotaxi, Link destLink, AVRequest nextRequest, //
			FuturePathContainer futurePathContainer, final double getTimeNow, double dropoffDurationPerStop,
			double pickupDurationPerStop, SharedAVMealType nextMealType) {
		super(robotaxi, destLink, futurePathContainer);
		this.robotaxi = robotaxi;
		this.nextRequest = nextRequest;
		this.getTimeNow = getTimeNow;
		this.dropoffDurationPerStop = dropoffDurationPerStop;
		this.pickupDurationPerStop = pickupDurationPerStop;
		this.nextMealType = nextMealType;
	}

	@Override
	void executeWithPath(VrpPathWithTravelData vrpPathWithTravelData) {
		final Schedule schedule = robotaxi.getSchedule();
		final AVStayTask avStayTask = (AVStayTask) schedule.getCurrentTask(); // <- implies that task is started
		final double scheduleEndTime = avStayTask.getEndTime(); // typically 108000.0
		GlobalAssert.that(scheduleEndTime == schedule.getEndTime());

		final AVDriveTask avDriveTask = new AVDriveTask(vrpPathWithTravelData);
		final double endDriveTask = avDriveTask.getEndTime();

		if (endDriveTask < scheduleEndTime) {

			GlobalAssert.that(nextRequest != null);
			GlobalAssert.that(nextMealType != null);
			
			final double starTimeNextMeal = vrpPathWithTravelData.getArrivalTime();

			final double endTimeNextMeal = nextMealType.equals(SharedAVMealType.DROPOFF)
					? starTimeNextMeal + dropoffDurationPerStop
					: starTimeNextMeal + pickupDurationPerStop;

			schedule.addTask(new AVDriveTask( //
					vrpPathWithTravelData, Arrays.asList(nextRequest)));

			Link destLink = null;

			// Add pickup or dropoff depending on meal type
			if (nextMealType.equals(SharedAVMealType.PICKUP)) {
				destLink = nextRequest.getFromLink();
				GlobalAssert.that(destination.equals(destLink));
				schedule.addTask(new AVPickupTask( //
						starTimeNextMeal, // start of dropoff
						endTimeNextMeal, destLink, // location of dropoff
						Arrays.asList(nextRequest)));
			} else if (nextMealType.equals(SharedAVMealType.DROPOFF)) {
				destLink = nextRequest.getToLink();
				GlobalAssert.that(destination.equals(destLink));
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
			reportExecutionBypass(endDriveTask - scheduleEndTime);
	}

}
