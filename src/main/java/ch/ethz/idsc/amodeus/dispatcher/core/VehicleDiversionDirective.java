/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.api.core.v01.network.Link;

/* package */ abstract class VehicleDiversionDirective extends FuturePathDirective {
    final AbstractRoboTaxi robotaxi;
    final Link destination;

    VehicleDiversionDirective(final AbstractRoboTaxi robotaxi, final Link destination, FuturePathContainer futurePathContainer) {
        super(futurePathContainer);
        this.robotaxi = robotaxi;
        this.destination = destination;
    }
}
