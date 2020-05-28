package ch.ethz.matsim.av.config.modal;

import org.matsim.core.config.ReflectiveConfigGroup;

public class TimingConfig extends ReflectiveConfigGroup {
    static public final String GROUP_NAME = "timing";

    static public final String MINIMUM_PICKUP_DURATION_PER_STOP = "minimumPickupDurationPerStop";
    static public final String PICKUP_DURATION_PER_PASSENGER = "pickupDurationPerPassenger";
    static public final String MINIMUM_DROPOFF_DURATION_PER_STOP = "minimumDropoffDurationPerStop";
    static public final String DROPOFF_DURATION_PER_PASSENGER = "dropoffDurationPerPassenger";

    private double minimumPickupDurationPerStop = 15.0;
    private double pickupDurationPerPassenger = 0.0;

    private double minimumDropoffDurationPerStop = 10.0;
    private double dropoffDurationPerPassenger = 0.0;

    public TimingConfig() {
        super(GROUP_NAME);
    }

    @StringGetter(MINIMUM_PICKUP_DURATION_PER_STOP)
    public double getMinimumPickupDurationPerStop() {
        return minimumPickupDurationPerStop;
    }

    @StringSetter(MINIMUM_PICKUP_DURATION_PER_STOP)
    public void setMinimumPickupDurationPerStop(double pickupDurationPerStop) {
        this.minimumPickupDurationPerStop = pickupDurationPerStop;
    }

    @StringGetter(PICKUP_DURATION_PER_PASSENGER)
    public double getPickupDurationPerPassenger() {
        return pickupDurationPerPassenger;
    }

    @StringSetter(PICKUP_DURATION_PER_PASSENGER)
    public void setPickupDurationPerPassenger(double pickupDurationPerPassenger) {
        this.pickupDurationPerPassenger = pickupDurationPerPassenger;
    }

    @StringGetter(MINIMUM_DROPOFF_DURATION_PER_STOP)
    public double getMinimumDropoffDurationPerStop() {
        return minimumDropoffDurationPerStop;
    }

    @StringSetter(MINIMUM_DROPOFF_DURATION_PER_STOP)
    public void setMinimumDropoffDurationPerStop(double dropoffDurationPerStop) {
        this.minimumDropoffDurationPerStop = dropoffDurationPerStop;
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
