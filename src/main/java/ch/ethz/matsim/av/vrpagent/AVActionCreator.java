package ch.ethz.matsim.av.vrpagent;

import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.run.DvrpMode;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic;
import org.matsim.contrib.dvrp.vrpagent.VrpLegFactory;
import org.matsim.contrib.dynagent.DynAction;
import org.matsim.contrib.dynagent.DynAgent;

import com.google.inject.Inject;

import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVPassengerDropoffActivity;
import ch.ethz.matsim.av.passenger.AVPassengerPickupActivity;
import ch.ethz.matsim.av.schedule.AVDropoffTask;
import ch.ethz.matsim.av.schedule.AVPickupTask;
import ch.ethz.matsim.av.schedule.AVStayTask;
import ch.ethz.matsim.av.schedule.AVTask;

public class AVActionCreator implements VrpAgentLogic.DynActionCreator {
	public static final String PICKUP_ACTIVITY_TYPE = "AVPickup";
	public static final String DROPOFF_ACTIVITY_TYPE = "AVDropoff";
	public static final String STAY_ACTIVITY_TYPE = "AVStay";

	@Inject
	@DvrpMode(AVModule.AV_MODE)
	private PassengerEngine passengerEngine;

	@Inject
	private VrpLegFactory legFactory;

	@Override
	public DynAction createAction(DynAgent dynAgent, DvrpVehicle vehicle, double now) {
		Task task = vehicle.getSchedule().getCurrentTask();
		if (task instanceof AVTask) {
			switch (((AVTask) task).getAVTaskType()) {
			case PICKUP:
				AVPickupTask mpt = (AVPickupTask) task;
				return new AVPassengerPickupActivity(passengerEngine, dynAgent, vehicle, mpt, mpt.getRequests(),
						PICKUP_ACTIVITY_TYPE, mpt.getEarliestDepartureTime());
			case DROPOFF:
				AVDropoffTask mdt = (AVDropoffTask) task;
				return new AVPassengerDropoffActivity(passengerEngine, dynAgent, vehicle, mdt, mdt.getRequests(),
						DROPOFF_ACTIVITY_TYPE);
			case DRIVE:
				return legFactory.create(vehicle);
			case STAY:
				return new AVStayActivity((AVStayTask) task);
			// return new VrpActivity(((AVStayTask)task).getName(), (StayTask) task);
			default:
				throw new IllegalStateException();
			}
		} else {
			throw new IllegalArgumentException();
		}
	}
}
