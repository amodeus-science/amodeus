package ch.ethz.refactoring.dvrp.activity;

import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dynagent.DynAgent;
import org.matsim.contrib.dynagent.FirstLastSimStepDynActivity;

import ch.ethz.matsim.av.passenger.AVRequest;

/** In theory, we could use MultiPassengerPickupActivity from DVRP here. However,
 * that class only either waits until a
 * predefined end time. Here, the intention was that leaving takes a certain amount of
 * time per passenger. However, this is not the case yet, but should be implemented and
 * then we should check if we can move this to DVRP!
 * 
 * TODO: Per-agent dropoff time */
public class AVPassengerDropoffActivity extends FirstLastSimStepDynActivity {
    public static final String ACTIVITY_TYPE = "AVDropoff";

    private final PassengerEngine passengerEngine;
    private final DynAgent driver;

    private final Map<Id<Request>, AVRequest> requests;
    private final double endTime;

    public AVPassengerDropoffActivity(PassengerEngine passengerEngine, DynAgent driver, DvrpVehicle vehicle, Map<Id<Request>, AVRequest> requests, double endTime) {
        super(ACTIVITY_TYPE);

        this.passengerEngine = passengerEngine;
        this.driver = driver;

        this.requests = requests;
        this.endTime = endTime;

        if (requests.size() > vehicle.getCapacity()) {
            // Number of requests exceeds number of seats
            throw new IllegalStateException();
        }
    }

    @Override
    public boolean isLastStep(double now) {
        return now >= endTime;
    }

    @Override
    protected void afterLastStep(double now) {
        for (PassengerRequest request : requests.values()) {
            passengerEngine.dropOffPassenger(driver, request, now);
        }
    }
}
