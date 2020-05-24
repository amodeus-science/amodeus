package ch.ethz.matsim.av.passenger;

import java.util.Set;

import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.passenger.PassengerPickupActivity;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dynagent.DynAgent;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;

import ch.ethz.matsim.av.config.modal.TimingConfig;

public class AVPassengerPickupActivity implements PassengerPickupActivity {
    private final DvrpVehicle vehicle;
    private final PassengerEngine passengerEngine;
    private final DynAgent driver;
    private final Set<AVRequest> requests;
    private final double pickupDurationPerPassenger;
    private final String activityType;

    private double endTime = 0.0;

    private int arrivedPassengers = 0;
    private int enteredPassengers = 0;

    private final double latestDepartureTime;

    public AVPassengerPickupActivity(PassengerEngine passengerEngine, DynAgent driver, DvrpVehicle vehicle, Set<AVRequest> requests, String activityType, double beginTime,
            double latestDepartureTime, TimingConfig timingConfig) {
        this.activityType = activityType;
        this.latestDepartureTime = latestDepartureTime;

        if (requests.size() > vehicle.getCapacity()) {
            // Number of requests exceeds number of seats
            throw new IllegalStateException();
        }

        this.passengerEngine = passengerEngine;
        this.driver = driver;
        this.requests = requests;
        this.vehicle = vehicle;

        this.pickupDurationPerPassenger = timingConfig.getPickupDurationPerPassenger();
        double pickupDurationPerStop = timingConfig.getPickupDurationPerStop();

        for (PassengerRequest request : requests) {
            if (passengerEngine.pickUpPassenger(this, driver, request, beginTime)) {
                arrivedPassengers++;
            }

            if (request.getEarliestStartTime() > latestDepartureTime) {
                latestDepartureTime = request.getEarliestStartTime();
            }
        }

        latestDepartureTime = Math.max(latestDepartureTime, beginTime + pickupDurationPerStop);
        endTime = beginTime + pickupDurationPerStop;

        updateEndTime(beginTime);
    }

    private void updateEndTime(double now) {
        if (enteredPassengers < arrivedPassengers) {
            // We still need to wait a bit, because people are entering

            int enteringPassengers = arrivedPassengers - enteredPassengers;
            enteredPassengers = arrivedPassengers;

            if (pickupDurationPerPassenger > 0.0) {
                endTime = Math.max(endTime, now + enteringPassengers * pickupDurationPerPassenger);
                return;
            }
        }

        if (enteredPassengers < requests.size()) {
            // We still need to wait, because some people have not arrived

            if (endTime == now) {
                endTime += 1.0;
            }
        } else {
            // All passengers have arrived and are in the vehicle

            if (enteredPassengers == vehicle.getCapacity()) {
                // Vehicle is full, let's depart whenever planned (we consider pickup time that
                // has been added before!)
                // Therefore no endTime = now !
            } else if (now < latestDepartureTime) {
                // Vehicle is not full and latest departure time is not reached
                endTime += 1.0;
            }
        }
    }

    @Override
    public double getEndTime() {
        return endTime;
    }

    @Override
    public void doSimStep(double now) {
        updateEndTime(now);
    }

    private PassengerRequest getRequestForPassenger(MobsimPassengerAgent passenger) {
        for (PassengerRequest request : requests) {
            if (passenger.getId().equals(request.getPassengerId()))
                return request;
        }

        return null;
    }

    @Override
    public void notifyPassengerIsReadyForDeparture(MobsimPassengerAgent passenger, double now) {
        PassengerRequest request = getRequestForPassenger(passenger);

        if (request == null) {
            throw new IllegalArgumentException("I am waiting for different passengers!");
        }

        if (passengerEngine.pickUpPassenger(this, driver, request, now)) {
            arrivedPassengers++;
        } else {
            throw new IllegalStateException("The passenger is not on the link or not available for departure!");
        }

        updateEndTime(now);
    }

    @Override
    public String getActivityType() {
        return activityType;
    }
}
