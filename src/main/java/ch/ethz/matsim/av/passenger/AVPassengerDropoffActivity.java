package ch.ethz.matsim.av.passenger;

import java.util.Set;

import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.schedule.StayTask;
import org.matsim.contrib.dynagent.DynActivity;
import org.matsim.contrib.dynagent.DynAgent;

import ch.ethz.matsim.av.config.modal.TimingConfig;

public class AVPassengerDropoffActivity implements DynActivity {
    private final PassengerEngine passengerEngine;
    private final DynAgent driver;
    private final Set<AVRequest> requests;
    private final String activityType;
    private final double endTime;

    public AVPassengerDropoffActivity(PassengerEngine passengerEngine, DynAgent driver, DvrpVehicle vehicle, StayTask dropoffTask, Set<AVRequest> requests, String activityType,
            TimingConfig timingConfig) {
        this.activityType = activityType;

        this.passengerEngine = passengerEngine;
        this.driver = driver;
        this.requests = requests;

        if (requests.size() > vehicle.getCapacity()) {
            // Number of requests exceeds number of seats
            throw new IllegalStateException();
        }

        double dropoffTimePerPassenger = timingConfig.getDropoffDurationPerPassenger();
        double dropoffTimePerStop = timingConfig.getDropoffDurationPerStop();

        endTime = Math.max(dropoffTask.getEndTime(), dropoffTask.getBeginTime() + dropoffTimePerStop + requests.size() * dropoffTimePerPassenger);
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
