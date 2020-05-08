package ch.ethz.matsim.av.routing.interaction;

import org.matsim.api.core.v01.network.Network;
import org.matsim.facilities.Facility;

import ch.ethz.matsim.av.config.AmodeusModeConfig;

public interface AVInteractionFinder {
    Facility findPickupFacility(Facility fromFacility, double departureTime);

    Facility findDropoffFacility(Facility toFacility, double departureTime);

    interface AVInteractionFinderFactory {
        AVInteractionFinder createInteractionFinder(AmodeusModeConfig operatorConfig, Network network);
    }
}
