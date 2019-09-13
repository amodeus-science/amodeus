/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.net;

import java.util.Map;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;

/* package */ enum VehicleContainerCompile {
    ;

    /** @param robotaxi
     * @param db
     * @return {@link VehicleContainer} filled with information for later viewing and storage
     *         in {@link SimulationObject} */
    public static VehicleContainer using(RoboTaxi robotaxi, MatsimAmodeusDatabase db) {
        VehicleContainer vc = new VehicleContainer();
        vc.vehicleIndex = db.getVehicleIndex(robotaxi);

        /** saving location trace and emptying in {@link RoboTaxi} */
        Map<Long, Link> localLocationTrace = robotaxi.flushLocationTrace();
        localLocationTrace.entrySet().forEach(e -> {
            vc.addLinkLocation(e.getKey(), db.getLinkIndex(e.getValue()));
        });

        /** saving status trace and emptying in {@link RoboTaxi} */
        Map<Long, RoboTaxiStatus> localStatusTrace = robotaxi.flushStatusTrace();
        localStatusTrace.entrySet().forEach(e -> {
            vc.addStatus(e.getKey(), e.getValue());
        });

        /** saving destination trace and emptying in {@link RoboTaxi} */
        Map<Long, Link> localDestinationTrace = robotaxi.flushDestinationTrace();
        localDestinationTrace.entrySet().forEach(e -> {
            vc.addDestination(e.getKey(), db.getLinkIndex(e.getValue()));
        });

        return vc;
    }
}
