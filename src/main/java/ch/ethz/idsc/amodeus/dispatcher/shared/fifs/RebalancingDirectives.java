/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.Map;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/** Helper Class to wrap rebalancing Directives. */
/* package */ class RebalancingDirectives {
    private final Map<RoboTaxi, Link> directives;

    RebalancingDirectives(Map<RoboTaxi, Link> directives) {
        this.directives = directives;
    }

    Map<RoboTaxi, Link> getDirectives() {
        return directives;
    }

    void addOtherDirectives(RebalancingDirectives rebalancingDirectives) {
        directives.putAll(rebalancingDirectives.getDirectives());
    }

    void removefromDirectives(RoboTaxi roboTaxi) {
        directives.remove(Objects.requireNonNull(roboTaxi));
    }
}
