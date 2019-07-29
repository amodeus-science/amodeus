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
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

class ParkingLP extends ParkingStrategyWithCapacity {
    private final long freeParkingPeriod = 5;

    private ParkingLPStaticHelper parkingLPStaticHelper;

    @Override
    public void setRunntimeParameters(ParkingCapacity avSpatialCapacityAmodeus, Network network, DistanceFunction distanceFunction) {
        super.setRunntimeParameters(avSpatialCapacityAmodeus, network, distanceFunction);
        this.parkingLPStaticHelper = new ParkingLPStaticHelper(avSpatialCapacityAmodeus, network);
    }

    @Override
    public Map<RoboTaxi, Link> keepFree(Collection<RoboTaxi> stayingRobotaxis, Collection<RoboTaxi> rebalancingRobotaxis, long now) {
        GlobalAssert.that(!Objects.isNull(distanceFunction));
        GlobalAssert.that(!Objects.isNull(parkingLPStaticHelper));

        if (now % freeParkingPeriod == 0) {

            Map<Link, Set<RoboTaxi>> linkStayTaxi = parkingLPStaticHelper.getOccupiedLinks(stayingRobotaxis);
            Map<Link, Set<RoboTaxi>> taxisToGo = parkingLPStaticHelper.getTaxisToGo(linkStayTaxi);
            Map<Link, Long> freeSpacesToGo = parkingLPStaticHelper.getFreeSpacesToGo(linkStayTaxi, rebalancingRobotaxis);

            if ((!taxisToGo.isEmpty()) & (!freeSpacesToGo.isEmpty())) {

                ParkingLPSolver parkingLPSolver = new ParkingLPSolver(taxisToGo, freeSpacesToGo, distanceFunction);

                return parkingLPSolver.returnSolution();
            }
            return new HashMap<>();
        }
        return new HashMap<>();
    }

}
