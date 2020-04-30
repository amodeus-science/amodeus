package ch.ethz.matsim.av.router;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.matsim.av.config.operator.RouterConfig;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

public interface AVRouter extends ParallelLeastCostPathCalculator {
	interface Factory {
		AVRouter createRouter(RouterConfig routerConfig, Network network);
	}
}
