package ch.ethz.matsim.av.passenger;

import java.util.Set;

import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.schedule.StayTask;
import org.matsim.contrib.dynagent.DynActivity;
import org.matsim.contrib.dynagent.DynAgent;

public class AVPassengerDropoffActivity implements DynActivity {
	private final PassengerEngine passengerEngine;
	private final DynAgent driver;
	private final Set<AVRequest> requests;
	private final String activityType;
	private final double endTime;

	public AVPassengerDropoffActivity(PassengerEngine passengerEngine, DynAgent driver, DvrpVehicle vehicle,
			StayTask dropoffTask, Set<AVRequest> requests, String activityType) {
		this.activityType = activityType;

		this.passengerEngine = passengerEngine;
		this.driver = driver;
		this.requests = requests;

		if (requests.size() > vehicle.getCapacity()) {
			// Number of requests exceeds number of seats
			throw new IllegalStateException();
		}

		double dropoffTimePerPassenger = 0.0;
		double dropoffTimePerStop = 0.0;

		if (requests.size() == 0) {
			throw new IllegalStateException("Received dropoff task without request");
		} else {
			// TODO @sebhoerl Not ideal since we go through the requests instead of passing it
			// directly
			AVRequest firstRequest = requests.iterator().next();

			dropoffTimePerPassenger = firstRequest.getOperator().getConfig().getTimingConfig()
					.getDropoffDurationPerPassenger();
			dropoffTimePerStop = firstRequest.getOperator().getConfig().getTimingConfig()
					.getDropoffDurationPerStop();
		}

		endTime = Math.max(dropoffTask.getEndTime(),
				dropoffTask.getBeginTime() + dropoffTimePerStop + requests.size() * dropoffTimePerPassenger);
	}

	@Override
	public void finalizeAction(double now) {
		for (PassengerRequest request : requests) {
			passengerEngine.dropOffPassenger(driver, request, now);
		}
	}

	@Override
	public String getActivityType() {
		return activityType;
	}

	@Override
	public double getEndTime() {
		return endTime;
	}

	@Override
	public void doSimStep(double now) {

	}
}
