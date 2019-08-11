/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;

/* package */ abstract class AbstractParkingDiffusionStrategy extends AbstractParkingStrategy {

    @Override
    public Map<RoboTaxi, Link> keepFree(Collection<RoboTaxi> stayingRobotaxis, //
            Collection<RoboTaxi> rebalancingRobotaxis, long now) {

        /** allow children classes to update necessary information to execute
         * the function destinationCompute */
        update(stayingRobotaxis, rebalancingRobotaxis, now);

        Map<Link, Set<RoboTaxi>> stayTaxis = StaticHelper.getOccupiedLinks(stayingRobotaxis);
        /** If there are too many vehicles on the link, send a sufficient number of them away
         * to random neighbors */

        Map<RoboTaxi, Link> directives = new HashMap<>();
        stayTaxis.entrySet().stream().forEach(e -> {
            Link link = e.getKey();
            Set<RoboTaxi> taxis = e.getValue();
            long capacity = parkingCapacity.getSpatialCapacity(link.getId());
            if (taxis.size() > capacity) {
                taxis.stream().limit(taxis.size() - capacity)//
                        .forEach(rt -> {
                            directives.put(rt, destinationCompute(rt));
                        });
            }
        });
        return directives;
    }

    protected abstract Link destinationCompute(RoboTaxi roboTaxi);

    protected abstract void update(Collection<RoboTaxi> stayingRobotaxis, //
            Collection<RoboTaxi> rebalancingRobotaxis, long now);

}
