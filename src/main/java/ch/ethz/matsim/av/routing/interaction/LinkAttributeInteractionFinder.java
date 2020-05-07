package ch.ethz.matsim.av.routing.interaction;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.LinkWrapperFacility;
import org.matsim.facilities.Facility;

import com.google.inject.Singleton;

import ch.ethz.matsim.av.config.operator.InteractionFinderConfig;
import ch.ethz.matsim.av.config.operator.OperatorConfig;

public class LinkAttributeInteractionFinder implements AVInteractionFinder {
    public final static String TYPE = "LinkAttribute";

    private final InteractionLinkData data;

    public LinkAttributeInteractionFinder(InteractionLinkData data) {
        this.data = data;
    }

    @Override
    public Facility findPickupFacility(Facility fromFacility, double departureTime) {
        if (fromFacility.getCoord() == null) {
            throw new IllegalStateException("Trying to find closest interaction facility, but no coords are given.");
        }

        return new LinkWrapperFacility(data.getClosestLink(fromFacility.getCoord()));
    }

    @Override
    public Facility findDropoffFacility(Facility toFacility, double departureTime) {
        return findPickupFacility(toFacility, departureTime);
    }

    @Singleton
    public static class Factory implements AVInteractionFinderFactory {
        @Override
        public AVInteractionFinder createInteractionFinder(OperatorConfig operatorConfig, Network network) {
            InteractionFinderConfig interactionConfig = operatorConfig.getInteractionFinderConfig();

            String attributeName = interactionConfig.getParams().getOrDefault("allowedLinkAttribute", "avAccessEgress");
            InteractionLinkData data = InteractionLinkData.fromAttribute(attributeName, network);

            if (data.getNumberOfLinks() == 0) {
                throw new IllegalStateException("Did not find any interaction point for operator: " + operatorConfig.getId());
            }

            return new LinkAttributeInteractionFinder(data);
        }
    }
}
