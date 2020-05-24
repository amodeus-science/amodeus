package ch.ethz.matsim.av.passenger;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.passenger.PassengerPickupActivity;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dynagent.DynAgent;
import org.matsim.contrib.dynagent.FirstLastSimStepDynActivity;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;

/** In theory, we could use MultiPassengerPickupActivity from DVRP here. However,
 * that class only either waits until all passengers have entered or until a
 * predefined end time. Here, entering actually takes a certain amount of time
 * *per passenger*. Need to check whether we can move this into DVRP. */
public class AVPassengerPickupActivity extends FirstLastSimStepDynActivity implements PassengerPickupActivity {
    public static final String ACTIVITY_TYPE = "AVPickup";

    private final PassengerEngine passengerEngine;
    private final DynAgent driver;

    private final Map<Id<Request>, AVRequest> requests;
    private final double durationPerPassenger;

    private final double expectedEndTime;
    private double passengerEndTime = Double.NEGATIVE_INFINITY;

    private int insidePassengers = 0;

    public AVPassengerPickupActivity(PassengerEngine passengerEngine, DynAgent driver, DvrpVehicle vehicle, Map<Id<Request>, AVRequest> requests, double expectedEndTime,
            double durationPerPassenger) {
        super(ACTIVITY_TYPE);

        this.expectedEndTime = expectedEndTime;
        this.durationPerPassenger = durationPerPassenger;

        this.passengerEngine = passengerEngine;
        this.driver = driver;

        this.requests = requests;

        if (requests.size() > vehicle.getCapacity()) {
            throw new IllegalStateException("Number of requests exceeds number of seats");
        }
    }

    @Override
    protected void beforeFirstStep(double now) {
        for (AVRequest request : requests.values()) {
            tryPerformPickup(request, now);
        }
    }

    private boolean tryPerformPickup(PassengerRequest request, double now) {
        if (passengerEngine.pickUpPassenger(this, driver, request, now)) {
            insidePassengers++;
            passengerEndTime = Math.max(passengerEndTime, now) + durationPerPassenger;
            return true;
        }

        return false;
    }

    @Override
    public boolean isLastStep(double now) {
        return now >= expectedEndTime && now >= passengerEndTime && insidePassengers == requests.size();
    }

    private PassengerRequest getRequestForPassenger(MobsimPassengerAgent passenger) {
        for (PassengerRequest request : requests.values()) {
            if (passenger.getId().equals(request.getPassengerId())) {
                return request;
            }
        }

        return null;
    }

    @Override
    public void notifyPassengerIsReadyForDeparture(MobsimPassengerAgent passenger, double now) {
        PassengerRequest request = getRequestForPassenger(passenger);

        if (request == null) {
            throw new IllegalArgumentException("I am waiting for different passengers!");
        }

        if (!tryPerformPickup(request, now)) {
            throw new IllegalStateException();
        }
    }
}
