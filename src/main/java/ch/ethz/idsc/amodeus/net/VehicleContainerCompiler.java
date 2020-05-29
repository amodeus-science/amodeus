/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.dispatcher.core.LinkStatusPair;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ enum VehicleContainerCompiler {
    ;

    /** @return a {@link VehicleContainer} that is used to save the result to the disk for current
     *         or later processing, e.g., in amod's the ScenarioViewer. The information is taken from the
     *         @param roboTaxi {@link RoboTaxi}, and the
     *         @param linkStatusPairs {@link List<LinkStatusPair>} containing all {@link Link}s passed since the last
     *         {@link SimulationObject} was saved with respective {@link RoboTaxiStatus}*/
    public static VehicleContainer compile(RoboTaxi roboTaxi, List<LinkStatusPair> linkStatusPairs) {
        VehicleContainer vc = new VehicleContainer();
        vc.vehicleIndex = roboTaxi.getId().index();
        GlobalAssert.that(!linkStatusPairs.isEmpty());
        vc.linkTrace = linkStatusPairs.stream().map(p -> p.link).map(Link::getId).mapToInt(Id::index).toArray();
        if (linkStatusPairs.get(linkStatusPairs.size() - 1).roboTaxiStatus == roboTaxi.getStatus()) {
            vc.statii = linkStatusPairs.stream().map(p -> p.roboTaxiStatus).toArray(RoboTaxiStatus[]::new);
        } else {
            List<RoboTaxiStatus> statii = linkStatusPairs.stream().map(p -> p.roboTaxiStatus).collect(Collectors.toList());
            statii.add(roboTaxi.getStatus());
            vc.statii = statii.toArray(RoboTaxiStatus[]::new);
        }
        Link toLink = roboTaxi.getCurrentDriveDestination();
        vc.destinationLinkIndex = Objects.requireNonNull(toLink).getId().index();
        return vc;
    }
}
