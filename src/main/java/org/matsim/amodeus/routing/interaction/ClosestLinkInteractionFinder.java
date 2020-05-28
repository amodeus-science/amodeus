package org.matsim.amodeus.routing.interaction;

import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.LinkWrapperFacility;
import org.matsim.facilities.Facility;

import com.google.inject.Singleton;

public class ClosestLinkInteractionFinder implements AVInteractionFinder {
    static public final String TYPE = "ClosestLink";

    private final Network network;

    public ClosestLinkInteractionFinder(Network network) {
        this.network = network;
    }

    @Override
    public Facility findPickupFacility(Facility fromFacility, double departureTime) {
        return findFacility(fromFacility);
    }

    @Override
    public Facility findDropoffFacility(Facility toFacility, double departureTime) {
        return findFacility(toFacility);
    }

    private Facility findFacility(Facility baseFacility) {
        if (baseFacility.getCoord() == null) {
            throw new IllegalStateException("Trying to find closest interaction facility, but not coords are given.");
        }

        return new LinkWrapperFacility(NetworkUtils.getNearestLink(network, baseFacility.getCoord()));
    }

    @Singleton
    public static class Factory implements AVInteractionFinderFactory {
        @Override
        public AVInteractionFinder createInteractionFinder(AmodeusModeConfig operatorConfig, Network network) {
            return new ClosestLinkInteractionFinder(network);
        }
    }
}
