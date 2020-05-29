package org.matsim.amodeus.routing;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.population.routes.RouteFactory;

import com.google.inject.Singleton;

@Singleton
public class AmodeusRouteFactory implements RouteFactory {
    @Override
    public AmodeusRoute createRoute(Id<Link> startLinkId, Id<Link> endLinkId) {
        return new AmodeusRoute(startLinkId, endLinkId);
    }

    @Override
    public String getCreatedRouteType() {
        return AmodeusRoute.AMODEUS_ROUTE;
    }
}
