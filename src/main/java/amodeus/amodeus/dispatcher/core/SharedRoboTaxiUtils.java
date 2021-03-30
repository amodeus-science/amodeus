/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import org.matsim.api.core.v01.network.Link;

import amodeus.amodeus.dispatcher.core.schedule.directives.Directive;
import amodeus.amodeus.dispatcher.core.schedule.directives.DriveDirective;
import amodeus.amodeus.dispatcher.core.schedule.directives.StopDirective;

/** Package internal helper class to do computations for {@link} {@link RoboTaxi} which
 * are in shared use. */
/* package */ enum SharedRoboTaxiUtils {
    ;

    /** @return the {@link} Link the (shared) {@link RoboTaxi} @param roboTaxi is travelling to next. */
    public static Link getStarterLink(RoboTaxi roboTaxi) {
        Directive starter = roboTaxi.getScheduleManager().getDirectives().get(0);

        if (starter instanceof StopDirective) {
            StopDirective stopDirective = (StopDirective) starter;

            if (stopDirective.isPickup()) {
                return stopDirective.getRequest().getFromLink();
            } else {
                return stopDirective.getRequest().getToLink();
            }
        } else {
            return ((DriveDirective) starter).getDestination();
        }
    }

    /** @return {@link RoboTaxiStatus} of {@link RoboTaxi} @param roboTaxi computed according
     *         to its {@link SharedMenu} */
    public static RoboTaxiStatus calculateStatusFromMenu(RoboTaxi roboTaxi) {
        if (roboTaxi.getScheduleManager().getDirectives().size() == 0) {
            return RoboTaxiStatus.STAY;
        }

        Directive starter = roboTaxi.getScheduleManager().getDirectives().get(0);

        if (roboTaxi.getOnBoardPassengers() > 0)
            return RoboTaxiStatus.DRIVEWITHCUSTOMER;

        if (starter instanceof StopDirective) {
            StopDirective stopDirective = (StopDirective) starter;

            if (stopDirective.isPickup()) {
                return RoboTaxiStatus.DRIVETOCUSTOMER;
            }
        } else if (starter instanceof DriveDirective) {
            boolean foundPickup = false;

            for (Directive directive : roboTaxi.getScheduleManager().getDirectives()) {
                if (directive instanceof StopDirective) {
                    StopDirective stopDirective = (StopDirective) directive;

                    if (stopDirective.isPickup()) {
                        foundPickup = true;
                    }
                }
            }

            if (foundPickup) {
                return RoboTaxiStatus.DRIVETOCUSTOMER;
            } else {
                return RoboTaxiStatus.REBALANCEDRIVE;
            }
        }

        throw new RuntimeException("We have a not Covered Status of the Robotaxi");
    }
}
