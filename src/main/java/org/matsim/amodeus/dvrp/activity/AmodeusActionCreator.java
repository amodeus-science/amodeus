package org.matsim.amodeus.dvrp.activity;

import org.matsim.amodeus.config.modal.TimingConfig;
import org.matsim.amodeus.dvrp.schedule.AmodeusStayTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask;
import org.matsim.amodeus.dvrp.schedule.AmodeusTaskTypes;
import org.matsim.amodeus.dvrp.schedule.AmodeusStopTask.StopType;
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

        if (AmodeusTaskTypes.STOP.equals(task.getTaskType())) {
            AmodeusStopTask stopTask = (AmodeusStopTask) task;

            double expectedEndTime = now + timingConfig.getMinimumPickupDurationPerStop();
            double durationPerPassenger = timingConfig.getPickupDurationPerPassenger();

            double dropoffEndTime = now
                    + Math.max(timingConfig.getMinimumDropoffDurationPerStop(), stopTask.getDropoffRequests().size() * timingConfig.getDropoffDurationPerPassenger());

            if (stopTask.getStopType() == StopType.Dropoff) {
                expectedEndTime = 0.0;
            }

            if (stopTask.getStopType() == StopType.Pickup) {
                dropoffEndTime = 0.0;
            }

            return new AmodeusStopActivity(passengerEngine, dynAgent, vehicle, stopTask.getPickupRequests(), expectedEndTime, durationPerPassenger, stopTask.getDropoffRequests(),
                    dropoffEndTime);
        } else if (AmodeusTaskTypes.DRIVE.equals(task.getTaskType())) {
            return legFactory.create(vehicle);
        } else if (AmodeusTaskTypes.STAY.equals(task.getTaskType())) {
            return new AmodeusStayActivity((AmodeusStayTask) task);
        } else {
            throw new IllegalStateException();
        }
    }
}
