package org.matsim.amodeus.dvrp.activity;

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
public class AmodeusStopActivity extends FirstLastSimStepDynActivity implements PassengerPickupActivity {
    public static final String ACTIVITY_TYPE = "STOP";

    private final PassengerEngine passengerEngine;
    private final DynAgent driver;

    private final Map<Id<Request>, PassengerRequest> pickupRequests;
    private final double durationPerPassenger;

    private final double expectedEndTime;
    private double passengerEndTime = Double.NEGATIVE_INFINITY;

    private int insidePassengers = 0;

    private final Map<Id<Request>, PassengerRequest> dropoffRequests;
    private final double dropoffEndTime;

    public AmodeusStopActivity(PassengerEngine passengerEngine, DynAgent driver, DvrpVehicle vehicle, Map<Id<Request>, PassengerRequest> pickupRequests, double expectedEndTime,
            double durationPerPassenger, Map<Id<Request>, PassengerRequest> dropoffRequests, double dropoffEndTime) {
        super(ACTIVITY_TYPE);

        this.expectedEndTime = expectedEndTime;
        this.durationPerPassenger = durationPerPassenger;

        this.passengerEngine = passengerEngine;
        this.driver = driver;

        this.pickupRequests = pickupRequests;
        this.dropoffRequests = dropoffRequests;
        this.dropoffEndTime = dropoffEndTime;

        if (pickupRequests.size() > vehicle.getCapacity()) {
            throw new IllegalStateException("Number of requests exceeds number of seats");
        }
        
        if (dropoffRequests.size() > vehicle.getCapacity()) {
            throw new IllegalStateException("Number of requests exceeds number of seats");
        }
    }

    @Override
    protected void beforeFirstStep(double now) {
        for (PassengerRequest request : pickupRequests.values()) {
            tryPerformPickup(request, now);
        }
    }
    
    @Override
    protected void afterLastStep(double now) {
        for (PassengerRequest request : dropoffRequests.values()) {
            passengerEngine.dropOffPassenger(driver, request, now);
        }
    }

    private boolean tryPerformPickup(PassengerRequest request, double now) {
        if (passengerEngine.tryPickUpPassenger(this, driver, request, now)) {
            insidePassengers++;
            passengerEndTime = Math.max(passengerEndTime, now) + durationPerPassenger;
            return true;
        }

        return false;
    }

    @Override
    public boolean isLastStep(double now) {
        return now >= dropoffEndTime && now >= expectedEndTime && now >= passengerEndTime && insidePassengers == pickupRequests.size();
    }

    private PassengerRequest getRequestForPassenger(MobsimPassengerAgent passenger) {
        for (PassengerRequest request : pickupRequests.values()) {
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
