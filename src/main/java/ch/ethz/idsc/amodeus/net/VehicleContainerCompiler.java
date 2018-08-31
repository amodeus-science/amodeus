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
    public static VehicleContainer compile(RoboTaxi robotaxi, MatsimStaticDatabase db) {
        VehicleContainer vehicleContainer = new VehicleContainer();
        vehicleContainer.vehicleIndex = db.getVehicleIndex(robotaxi);
        final Link fromLink = robotaxi.getLastKnownLocation();
        GlobalAssert.that(Objects.nonNull(fromLink));
        vehicleContainer.linkIndex = db.getLinkIndex_id(fromLink.getId().toString()); // TODO changed due to MatsimStaticDatabase problem
        vehicleContainer.roboTaxiStatus = robotaxi.getStatus();
        Link toLink = robotaxi.getCurrentDriveDestination();
        vehicleContainer.destinationLinkIndex = db.getLinkIndex_id(Objects.requireNonNull(toLink).getId().toString()); // TODO changed due to MatsimStaticDatabase problem
        return vehicleContainer;
    }

}
