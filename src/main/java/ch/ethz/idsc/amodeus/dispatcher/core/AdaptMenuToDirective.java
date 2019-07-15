/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Optional;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;

/* package */ enum AdaptMenuToDirective {
    ;

    public static void now(RoboTaxi roboTaxi, FuturePathFactory futurePathFactory, double now, //
            EventsManager eventsManager, boolean reRoute) {
        // Check that we are not already on the link of the redirectino (this can only happen if a command was given in redispatch to the current location)
        removeRedirectionToDivertableLocationInBeginning(roboTaxi);

        Optional<Link> link = RetrieveToLink.forShared(roboTaxi, now);
        if (link.isPresent()) {
            SetSharedRoboTaxiDiversion.now(roboTaxi, link.get(), futurePathFactory, now, eventsManager, reRoute);
        }

    }

    private static void removeRedirectionToDivertableLocationInBeginning(RoboTaxi roboTaxi) {
        while (NextCourseIsRedirectToCurrentLink.check(roboTaxi)) {
            roboTaxi.finishRedirection();
        }
    }

}
