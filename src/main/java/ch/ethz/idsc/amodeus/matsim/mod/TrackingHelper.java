/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.matsim.mod;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.schedule.DriveTask;
import org.matsim.contrib.dvrp.tracker.OnlineDriveTaskTracker;
import org.matsim.contrib.dvrp.tracker.OnlineTrackerListener;
import org.matsim.contrib.dvrp.vrpagent.VrpLeg;
import org.matsim.contrib.dvrp.vrpagent.VrpLegFactory;
import org.matsim.core.mobsim.framework.MobsimTimer;

/* package */enum TrackingHelper {
    ;
    static VrpLegFactory createLegCreatorWithIDSCTracking(OnlineTrackerListener optimizer, MobsimTimer timer) {
        return new VrpLegFactory() {
            @Override
            public VrpLeg create(DvrpVehicle vehicle) {
                DriveTask driveTask = (DriveTask) vehicle.getSchedule().getCurrentTask();
                VrpLeg leg = new VrpLeg(TransportMode.car, driveTask.getPath());

                OnlineDriveTaskTracker idscTracker = new AmodeusDriveTaskTracker(vehicle, leg, optimizer, timer);
                driveTask.initTaskTracker(idscTracker);
                leg.initOnlineTracking(idscTracker);

                return leg;
            }
        };
    }
}
