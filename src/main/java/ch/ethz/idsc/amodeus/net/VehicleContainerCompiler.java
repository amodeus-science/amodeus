/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.util.List;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ enum VehicleContainerCompiler {
    ;

    /** @return a {@link VehicleContainer} that is used to save the result to the disk for current
     *         or later processing, e.g., in the {@link ScenarioViewer}. The information is taken from the
     *         {@link RoboTaxi} @param roboTaxi, the {@link List} {@linkLink} @param linkTrace contains all
     *         {@link Link}s passed since the last {@link SimulationObject} was saved, passed to the
     *         {@link VehicleObject} as int indexes according to the @param db */
    public static VehicleContainer compile(RoboTaxi roboTaxi, List<Link> linkTrace, MatsimAmodeusDatabase db) {
        VehicleContainer vc = new VehicleContainer();
        vc.vehicleIndex = db.getVehicleIndex(roboTaxi);
        GlobalAssert.that(!linkTrace.isEmpty());
        vc.linkTrace = linkTrace.stream().mapToInt(db::getLinkIndex).toArray();
        vc.roboTaxiStatus = roboTaxi.getStatus();
        Link toLink = roboTaxi.getCurrentDriveDestination();
        vc.destinationLinkIndex = db.getLinkIndex(Objects.requireNonNull(toLink));
        return vc;
    }

}
