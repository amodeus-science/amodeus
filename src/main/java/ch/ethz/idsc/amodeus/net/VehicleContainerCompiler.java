/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

enum VehicleContainerCompiler {
    ;

    /** @param robotaxi
     * @param db
     * @return {@link VehicleContainer} filled with information for later viewing and storage
     *         in {@link SimulationObject} */
    public static VehicleContainer compile(RoboTaxi robotaxi, MatsimAmodeusDatabase db) {
        VehicleContainer vc = new VehicleContainer();
        vc.vehicleIndex = db.getVehicleIndex(robotaxi);
        final Link fromLink = robotaxi.flushLastKnownLocation();
        GlobalAssert.that(Objects.nonNull(fromLink));
        vc.linkIndex = db.getLinkIndex(fromLink);
        vc.roboTaxiStatus = robotaxi.getStatus();
        Link toLink = robotaxi.getCurrentDriveDestination();
        vc.destinationLinkIndex = db.getLinkIndex(Objects.requireNonNull(toLink));
        return vc;
    }

}
