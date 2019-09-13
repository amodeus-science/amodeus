/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ enum VehicleContainerCompile {
    ;

    /** @param robotaxi
     * @param db
     * @return {@link VehicleContainer} filled with information for later viewing and storage
     *         in {@link SimulationObject} */
    public static VehicleContainer using(RoboTaxi robotaxi, MatsimAmodeusDatabase db, long now) {
        VehicleContainer vc = new VehicleContainer();
        vc.vehicleIndex = db.getVehicleIndex(robotaxi);
        final Link location = robotaxi.getLastKnownLocation();
        GlobalAssert.that(Objects.nonNull(location));
        vc.addLinkLocation(now, db.getLinkIndex(location));
        vc.roboTaxiStatus = robotaxi.getStatus();
        Link toLink = robotaxi.getCurrentDriveDestination();
        vc.destinationLinkIndex = db.getLinkIndex(Objects.requireNonNull(toLink));
        return vc;
    }

}
