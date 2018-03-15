/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.optimizer.VrpOptimizerWithOnlineTracking;
import org.matsim.contrib.dvrp.schedule.DriveTask;
import org.matsim.contrib.dvrp.tracker.OnlineDriveTaskTracker;
import org.matsim.contrib.dvrp.vrpagent.VrpLeg;
import org.matsim.contrib.dvrp.vrpagent.VrpLegs.LegCreator;
import org.matsim.core.mobsim.framework.MobsimTimer;

/* package */enum TrackingHelper {
    ;
    static LegCreator createLegCreatorWithIDSCTracking(VrpOptimizerWithOnlineTracking optimizer, MobsimTimer timer) {
        return new LegCreator() {
            @Override
            public VrpLeg createLeg(Vehicle vehicle) {
                DriveTask driveTask = (DriveTask) vehicle.getSchedule().getCurrentTask();
                VrpLeg leg = new VrpLeg(driveTask.getPath());

                OnlineDriveTaskTracker idscTracker = new AmodeusDriveTaskTracker(vehicle, leg, optimizer, timer);
                driveTask.initTaskTracker(idscTracker);
                leg.initOnlineTracking(idscTracker);

                return leg;
            }
        };
    }
}
