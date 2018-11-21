/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.kockelman;

import java.util.Map;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/* package */ class RebalancingDirectives {
    private final Map<RoboTaxi, Link> directives;

    /* package */ RebalancingDirectives(Map<RoboTaxi, Link> directives) {
        this.directives = directives;
    }

    /* package */ Map<RoboTaxi, Link> getDirectives() {
        return directives;
    }

    /* package */ void removefromDirectives(RoboTaxi roboTaxi) {
        if (directives.containsKey(roboTaxi)) {
            directives.remove(roboTaxi);
        }
    }
}
