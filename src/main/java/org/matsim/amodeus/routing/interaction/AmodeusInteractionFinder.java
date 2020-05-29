package org.matsim.amodeus.routing.interaction;

import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.network.Network;
import org.matsim.facilities.Facility;

public interface AmodeusInteractionFinder {
    Facility findPickupFacility(Facility fromFacility, double departureTime);

    Facility findDropoffFacility(Facility toFacility, double departureTime);

    interface AVInteractionFinderFactory {
        AmodeusInteractionFinder createInteractionFinder(AmodeusModeConfig operatorConfig, Network network);
    }
}
