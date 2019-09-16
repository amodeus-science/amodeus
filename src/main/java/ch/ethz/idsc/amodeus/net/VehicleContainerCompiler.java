/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.util.List;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ enum VehicleContainerCompiler {
    ;

    /** @return {@link VehicleContainer} filled with information for later viewing and storage
     *         in {@link SimulationObject}, information is taken from {@link RoboTaxi} @param roboTaxi
     *         and the {@link MatsimAmodeusDatabase} @param db */
    public static VehicleContainer compile(RoboTaxi roboTaxi, List<Link> lastLoc, MatsimAmodeusDatabase db) {
        VehicleContainer vc = new VehicleContainer();
        vc.vehicleIndex = db.getVehicleIndex(roboTaxi);
        GlobalAssert.that(!lastLoc.isEmpty());
        vc.linkIndex = db.getLinkIndex(lastLoc.get(lastLoc.size()-1));
        vc.roboTaxiStatus = roboTaxi.getStatus();
        Link toLink = roboTaxi.getCurrentDriveDestination();
        vc.destinationLinkIndex = db.getLinkIndex(Objects.requireNonNull(toLink));
        return vc;
    }

}
