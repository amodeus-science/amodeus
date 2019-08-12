/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.parking.strategies;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacity;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;

/* package */ class ParkingLP extends AbstractParkingStrategy {
    private ParkingLPHelper parkingLPHelper;

    @Override
    public void setRuntimeParameters(ParkingCapacity parkingCapacity, Network network, //
            DistanceFunction distanceFunction) {
        super.setRuntimeParameters(parkingCapacity, network, distanceFunction);
        this.parkingLPHelper = new ParkingLPHelper(parkingCapacity, network);
    }

    @Override
    public Map<RoboTaxi, Link> keepFree(Collection<RoboTaxi> stayingRobotaxis, //
            Collection<RoboTaxi> rebalancingRobotaxis, long now) {
        Objects.requireNonNull(distanceFunction);
        Objects.requireNonNull(parkingLPHelper);
        Map<Link, Set<RoboTaxi>> linkStayTaxi = StaticHelper.getOccupiedLinks(stayingRobotaxis);
        Map<Link, Set<RoboTaxi>> taxisToGo = parkingLPHelper.getTaxisToGo(linkStayTaxi);
        if (!taxisToGo.isEmpty()) { /** if there are ongoing parking violations, resolve, otherwise skip */
            Map<Link, Long> freeSpacesToGo = parkingLPHelper.getFreeSpacesToGo(linkStayTaxi, //
                    StaticHelper.getDestinationCount(rebalancingRobotaxis));
            if (!freeSpacesToGo.isEmpty()) /** skip any action if no free spaces */
                return (new ParkingLPSolver<Link>(taxisToGo, freeSpacesToGo, //
                        (l1, l2) -> distanceFunction.getDistance(l1, l2))).returnSolution();
        }
        return new HashMap<>();
    }

}
