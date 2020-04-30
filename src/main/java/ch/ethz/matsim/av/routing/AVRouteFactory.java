package ch.ethz.matsim.av.routing;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.population.routes.RouteFactory;

import com.google.inject.Singleton;

@Singleton
public class AVRouteFactory implements RouteFactory {
	@Override
	public AVRoute createRoute(Id<Link> startLinkId, Id<Link> endLinkId) {
		return new AVRoute(startLinkId, endLinkId);
	}

	@Override
	public String getCreatedRouteType() {
		return AVRoute.AV_ROUTE;
	}
}
