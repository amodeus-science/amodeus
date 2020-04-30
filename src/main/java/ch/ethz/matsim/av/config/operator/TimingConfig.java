package ch.ethz.matsim.av.config.operator;

import org.matsim.core.config.ReflectiveConfigGroup;

public class TimingConfig extends ReflectiveConfigGroup {
	static public final String GROUP_NAME = "timing";

	static public final String PICKUP_DURATION_PER_STOP = "pickupDurationPerStop";
	static public final String PICKUP_DURATION_PER_PASSENGER = "pickupDurationPerPassenger";
	static public final String DROPOFF_DURATION_PER_STOP = "dropoffDurationPerStop";
	static public final String DROPOFF_DURATION_PER_PASSENGER = "dropoffDurationPerPassenger";

	private double pickupDurationPerStop = 15.0;
	private double pickupDurationPerPassenger = 0.0;

	private double dropoffDurationPerStop = 10.0;
	private double dropoffDurationPerPassenger = 0.0;

	public TimingConfig() {
		super(GROUP_NAME);
	}

	@StringGetter(PICKUP_DURATION_PER_STOP)
	public double getPickupDurationPerStop() {
		return pickupDurationPerStop;
	}

	@StringSetter(PICKUP_DURATION_PER_STOP)
	public void setPickupDurationPerStop(double pickupDurationPerStop) {
		this.pickupDurationPerStop = pickupDurationPerStop;
	}

	@StringGetter(PICKUP_DURATION_PER_PASSENGER)
	public double getPickupDurationPerPassenger() {
		return pickupDurationPerPassenger;
	}

	@StringSetter(PICKUP_DURATION_PER_PASSENGER)
	public void setPickupDurationPerPassenger(double pickupDurationPerPassenger) {
		this.pickupDurationPerPassenger = pickupDurationPerPassenger;
	}

	@StringGetter(DROPOFF_DURATION_PER_STOP)
	public double getDropoffDurationPerStop() {
		return dropoffDurationPerStop;
	}

	@StringSetter(DROPOFF_DURATION_PER_STOP)
	public void setDropoffDurationPerStop(double dropoffDurationPerStop) {
		this.dropoffDurationPerStop = dropoffDurationPerStop;
	}

	@StringGetter(DROPOFF_DURATION_PER_PASSENGER)
	public double getDropoffDurationPerPassenger() {
		return dropoffDurationPerPassenger;
	}

	@StringSetter(DROPOFF_DURATION_PER_PASSENGER)
	public void setDropoffDurationPerPassenger(double dropoffDurationPerPassenger) {
		this.dropoffDurationPerPassenger = dropoffDurationPerPassenger;
	}
}
