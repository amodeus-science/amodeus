/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.Map;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/**
 * Helper Class to wrap rebalancing Directives.
 */
/* package */ class RebalancingDirectives {
    private final Map<RoboTaxi, Link> directives;

    /* package */ RebalancingDirectives(Map<RoboTaxi, Link> directives) {
        this.directives = directives;
    }

    /* package */ Map<RoboTaxi, Link> getDirectives() {
        return directives;
    }

    /* package */ void addOtherDirectives(RebalancingDirectives rebalancingDirectives) {
        directives.putAll(rebalancingDirectives.getDirectives());
    }

    /* package */ void removefromDirectives(RoboTaxi roboTaxi) {
        if (directives.containsKey(roboTaxi)) {
            directives.remove(roboTaxi);
        }
    }
}
