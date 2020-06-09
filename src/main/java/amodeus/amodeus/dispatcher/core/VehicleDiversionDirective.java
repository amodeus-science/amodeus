/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import org.matsim.api.core.v01.network.Link;

/* package */ abstract class VehicleDiversionDirective extends FuturePathDirective {
    final RoboTaxi roboTaxi;
    final Link destination;

    VehicleDiversionDirective(RoboTaxi roboTaxi, Link destination, FuturePathContainer futurePathContainer) {
        super(futurePathContainer);
        this.roboTaxi = roboTaxi;
        this.destination = destination;
    }
}
