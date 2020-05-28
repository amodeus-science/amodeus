package org.matsim.amodeus.dvrp.activity;

import org.matsim.amodeus.config.modal.TimingConfig;
import org.matsim.amodeus.dvrp.schedule.AmodeusDropoffTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusPickupTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusTaskType;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerEngine;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.vrpagent.VrpAgentLogic;
import org.matsim.contrib.dvrp.vrpagent.VrpLegFactory;
import org.matsim.contrib.dynagent.DynAction;
import org.matsim.contrib.dynagent.DynAgent;

public class AmodeusActionCreator implements VrpAgentLogic.DynActionCreator {
    private final PassengerEngine passengerEngine;
    private final VrpLegFactory legFactory;
    private final TimingConfig timingConfig;

    public AmodeusActionCreator(PassengerEngine passengerEngine, VrpLegFactory legFactory, TimingConfig timingConfig) {
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

            return new AmodeusPickupActivity(passengerEngine, dynAgent, vehicle, mpt.getRequests(), expectedEndTime, durationPerPassenger);
        case DROPOFF:
            AmodeusDropoffTask mdt = (AmodeusDropoffTask) task;
            double endTime = now + Math.max(timingConfig.getMinimumDropoffDurationPerStop(), mdt.getRequests().size() * timingConfig.getDropoffDurationPerPassenger());

            return new AmodeusDropoffActivity(passengerEngine, dynAgent, vehicle, mdt.getRequests(), endTime);
        case DRIVE:
            return legFactory.create(vehicle);
        case STAY:
            return new AmodeusStayActivity((AmodeusStayTask) task);
        default:
            throw new IllegalStateException();
        }
    }
}
