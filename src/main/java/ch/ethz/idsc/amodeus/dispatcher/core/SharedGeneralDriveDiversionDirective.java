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
/* package */ final class SharedGeneralDriveDiversionDirective extends VehicleDiversionDirective {
	final SharedRoboTaxi robotaxi;
	final AVRequest nextRequest;
	final double getTimeNow;
	final double dropoffDurationPerStop;
	final double pickupDurationPerStop;
	final SharedAVMealType nextMealType;

	SharedGeneralDriveDiversionDirective(SharedRoboTaxi robotaxi, Link destLink, AVRequest nextRequest, //
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
		final AVDriveTask avDriveTask = (AVDriveTask) schedule.getCurrentTask(); // <- implies that task is started
		final AVStayTask avStayTask = (AVStayTask) Schedules.getLastTask(schedule);
		final double scheduleEndTime = avStayTask.getEndTime();

		TaskTracker taskTracker = avDriveTask.getTaskTracker();
		AmodeusDriveTaskTracker onlineDriveTaskTrackerImpl = (AmodeusDriveTaskTracker) taskTracker;
		final int diversionLinkIndex = onlineDriveTaskTrackerImpl.getDiversionLinkIndex();
		final int lengthOfDiversion = vrpPathWithTravelData.getLinkCount();
		OnlineDriveTaskTracker onlineDriveTaskTracker = (OnlineDriveTaskTracker) taskTracker;
		final double newEndTime = vrpPathWithTravelData.getArrivalTime();
		final double starTimeNextMeal = newEndTime;
		final double endTimeNextMeal = nextMealType.equals(SharedAVMealType.DROPOFF)
				? starTimeNextMeal + dropoffDurationPerStop
				: starTimeNextMeal + pickupDurationPerStop;

		if (newEndTime < scheduleEndTime) {

			try {
				GlobalAssert.that(VrpPathUtils.isConsistent(avDriveTask.getPath()));

				final int lengthOfCombination = avDriveTask.getPath().getLinkCount();
				// System.out.println(String.format("[@%d of %d]", diversionLinkIndex,
				// lengthOfCombination));
				if (diversionLinkIndex + lengthOfDiversion != lengthOfCombination)
					throw new RuntimeException(
							"mismatch " + diversionLinkIndex + "+" + lengthOfDiversion + " != " + lengthOfCombination);

				// FIXME
				GlobalAssert.that(avDriveTask.getEndTime() == newEndTime);

				// Remove all pending tasks in the future
				while (Schedules.getLastTask(schedule).getEndTime() != schedule.getCurrentTask().getEndTime()) {
					schedule.removeLastTask();
				}

				// Add new drive task
				// schedule.addTask(new AVDriveTask( //
				// vrpPathWithTravelData, Arrays.asList(nextRequest)));
				Link destLink = null;

				if (nextRequest != null) {
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

				} else {
					destLink = vrpPathWithTravelData.getToLink();
				}
				GlobalAssert.that(destLink != null);

				ScheduleUtils.makeWhole(robotaxi, endTimeNextMeal, scheduleEndTime, destLink);

				// jan: following computation is mandatory for the internal scoring
				// function
				final double distance = VrpPathUtils.getDistance(vrpPathWithTravelData);
				nextRequest.getRoute().setDistance(distance);

			} catch (Exception e) {
				System.err.println("Robotaxi ID: " + robotaxi.getId().toString());
				System.err.println("====================================");
				System.err.println("Found problem with diversionLinkIdx!");
				System.err.println("====================================");
			}

		} else
			reportExecutionBypass(newEndTime - scheduleEndTime);
	}

}
