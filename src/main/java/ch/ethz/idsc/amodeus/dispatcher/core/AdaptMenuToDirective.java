/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import org.matsim.core.api.experimental.events.EventsManager;

/* package */ enum AdaptMenuToDirective {
    ;

    public static void now(RoboTaxi roboTaxi, FuturePathFactory futurePathFactory, double now, //
            EventsManager eventsManager, boolean reRoute) {
        // Check that we are not already on the link of the redirection (this can only happen if a command was given in redispatch to the current location)
        removeRedirectionToDivertableLocationInBeginning(roboTaxi);

        RetrieveToLink.forShared(roboTaxi, now).ifPresent(link -> //
                SetSharedRoboTaxiDiversion.now(roboTaxi, link, futurePathFactory, now, eventsManager, reRoute));
    }

    private static void removeRedirectionToDivertableLocationInBeginning(RoboTaxi roboTaxi) {
        while (NextCourseIsRedirectToCurrentLink.check(roboTaxi))
            roboTaxi.finishRedirection();
    }

}
