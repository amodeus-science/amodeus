/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.matsim.av.passenger.AVRequest;

/* package */ class GridRebalancing {
    private final RebalancingExecutor blocks;

    /* package */ GridRebalancing(Network network, TravelTimeCalculatorCached timeDb, int minNumberRobotaxis, double historicalDataTime, double predictedTime,
            double gridDistance) {
        this.blocks = new RebalancingExecutor(network, timeDb, minNumberRobotaxis, historicalDataTime, predictedTime, gridDistance);
    }

    /* package */ RebalancingDirectives getRebalancingDirectives(double now, Set<RoboTaxi> allAvailableRobotaxisforRebalance, Set<AVRequest> allUnassignedAVRequests,
            Set<Link> allRequestLinksLastHour) {
        return blocks.getRebalancingDirectives(now, allRequestLinksLastHour, allUnassignedAVRequests, allAvailableRobotaxisforRebalance);
    }

}
