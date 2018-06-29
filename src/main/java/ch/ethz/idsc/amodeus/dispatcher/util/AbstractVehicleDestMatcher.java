/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

public abstract class AbstractVehicleDestMatcher {

    public final Map<RoboTaxi, AVRequest> matchAVRequest( //
            Collection<RoboTaxi> vehicleLinkPairs, //
            Collection<AVRequest> avRequests) {
        if (vehicleLinkPairs.isEmpty() || avRequests.isEmpty())
            return Collections.emptyMap();
        return protected_matchAVRequest(vehicleLinkPairs, avRequests);
    }

    protected abstract Map<RoboTaxi, AVRequest> protected_matchAVRequest( //
            Collection<RoboTaxi> vehicleLinkPairs, //
            Collection<AVRequest> links //
    );

    public final Map<RoboTaxi, Link> matchLink( //
            Collection<RoboTaxi> vehicleLinkPairs, //
            Collection<Link> destinations) {
        if (vehicleLinkPairs.isEmpty() || destinations.isEmpty())
            return Collections.emptyMap();
        return protected_matchLink(vehicleLinkPairs, destinations);
    }

    protected abstract Map<RoboTaxi, Link> protected_matchLink( //
            Collection<RoboTaxi> vehicleLinkPairs, //
            Collection<Link> links //
    );

}
