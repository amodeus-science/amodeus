/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/** Helper Class to wrap rebalancing Directives. */
public class RebalancingDirectives {
    private final Map<RoboTaxi, Link> directives;

    public RebalancingDirectives() {
        this(new HashMap<>());
    }

    public RebalancingDirectives(Map<RoboTaxi, Link> directives) {
        this.directives = directives;
    }

    public Map<RoboTaxi, Link> getDirectives() {
        return directives;
    }

    public void add(RoboTaxi roboTaxi, Link link) {
        directives.put(roboTaxi, link);
    }

    public void addOtherDirectives(RebalancingDirectives rebalancingDirectives) {
        directives.putAll(rebalancingDirectives.getDirectives());
    }

    public void removefromDirectives(RoboTaxi roboTaxi) {
        directives.remove(Objects.requireNonNull(roboTaxi));
    }
}
