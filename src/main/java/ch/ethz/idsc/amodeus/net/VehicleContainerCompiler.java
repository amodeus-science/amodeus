/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.util.List;
import java.util.Objects;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ enum VehicleContainerCompiler {
    ;

    /** @return a {@link VehicleContainer} that is used to save the result to the disk for current
     *         or later processing, e.g., in amod's the ScenarioViewer. The information is taken from the
     *         @param roboTaxi {@link RoboTaxi}, and the
     *         @param linkTrace {@link List<Link>} containing all {@link Link}s passed since the last
     *         {@link SimulationObject} was saved */
    public static VehicleContainer compile(RoboTaxi roboTaxi, List<Link> linkTrace) {
        VehicleContainer vc = new VehicleContainer();
        vc.vehicleIndex = roboTaxi.getId().index();
        GlobalAssert.that(!linkTrace.isEmpty());
        vc.linkTrace = linkTrace.stream().map(Link::getId).mapToInt(Id::index).toArray();
        vc.roboTaxiStatus = roboTaxi.getStatus();
        Link toLink = roboTaxi.getCurrentDriveDestination();
        vc.destinationLinkIndex = Objects.requireNonNull(toLink).getId().index();
        return vc;
    }
}
