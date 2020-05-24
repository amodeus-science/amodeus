package ch.ethz.matsim.av.vrpagent;

import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic;
import org.matsim.contrib.dvrp.vrpagent.VrpLegFactory;
import org.matsim.contrib.dynagent.DynAction;
import org.matsim.contrib.dynagent.DynAgent;

import ch.ethz.matsim.av.config.modal.TimingConfig;
import ch.ethz.matsim.av.passenger.AVPassengerDropoffActivity;
import ch.ethz.matsim.av.passenger.AVPassengerPickupActivity;
import ch.ethz.refactoring.schedule.AmodeusDropoffTask;
import ch.ethz.refactoring.schedule.AmodeusPickupTask;
import ch.ethz.refactoring.schedule.AmodeusStayTask;
import ch.ethz.refactoring.schedule.AmodeusTaskType;

public class AVActionCreator implements VrpAgentLogic.DynActionCreator {
    public static final String STAY_ACTIVITY_TYPE = "AVStay";

    private final PassengerEngine passengerEngine;
    private final VrpLegFactory legFactory;
    private final TimingConfig timingConfig;

    public AVActionCreator(PassengerEngine passengerEngine, VrpLegFactory legFactory, TimingConfig timingConfig) {
        this.passengerEngine = passengerEngine;
        this.legFactory = legFactory;
        this.timingConfig = timingConfig;
    }

    @Override
    public DynAction createAction(DynAgent dynAgent, DvrpVehicle vehicle, double now) {
        Task task = vehicle.getSchedule().getCurrentTask();

        switch ((AmodeusTaskType) task.getTaskType()) {
        case PICKUP:
            AmodeusPickupTask mpt = (AmodeusPickupTask) task;

            double expectedEndTime = now + timingConfig.getMinimumPickupDurationPerStop();
            double durationPerPassenger = timingConfig.getPickupDurationPerPassenger();

            return new AVPassengerPickupActivity(passengerEngine, dynAgent, vehicle, mpt.getRequests(), expectedEndTime, durationPerPassenger);
        case DROPOFF:
            AmodeusDropoffTask mdt = (AmodeusDropoffTask) task;
            double endTime = now + Math.max(timingConfig.getMinimumDropoffDurationPerStop(), mdt.getRequests().size() * timingConfig.getDropoffDurationPerPassenger());

            return new AVPassengerDropoffActivity(passengerEngine, dynAgent, vehicle, mdt.getRequests(), endTime);
        case DRIVE:
            return legFactory.create(vehicle);
        case STAY:
            return new AVStayActivity((AmodeusStayTask) task);
        default:
            throw new IllegalStateException();
        }
    }
}
