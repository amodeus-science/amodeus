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
import ch.ethz.idsc.amodeus.lp.RedistributionProblemSolver;
import ch.ethz.idsc.amodeus.lp.RedistributionProblemHelper;
import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacity;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;

/* package */ class ParkingFlow extends AbstractParkingStrategy {
    private ParkingFlowHelper parkingLPHelper;

    @Override
    public void setRuntimeParameters(ParkingCapacity parkingCapacity, Network network, //
            DistanceFunction distanceFunction) {
        super.setRuntimeParameters(parkingCapacity, network, distanceFunction);
        this.parkingLPHelper = new ParkingFlowHelper(parkingCapacity, network);
    }

    @Override
    public Map<RoboTaxi, Link> keepFree(Collection<RoboTaxi> stayingRobotaxis, //
            Collection<RoboTaxi> rebalancingRobotaxis, long now) {
        Objects.requireNonNull(distanceFunction);
        Objects.requireNonNull(parkingLPHelper);
        Map<Link, Set<RoboTaxi>> linkStayTaxi = StaticHelper.getOccupiedLinks(stayingRobotaxis);
        Map<Link, Set<RoboTaxi>> taxisToGo = parkingLPHelper.getTaxisToGo(linkStayTaxi);
        /** if there are ongoing parking violations, resolve, otherwise skip */
        if (!taxisToGo.isEmpty()) {
            Map<Link, Integer> freeSpacesToGo = parkingLPHelper.getFreeSpacesToGo(linkStayTaxi, //
                    StaticHelper.getDestinationCount(rebalancingRobotaxis));
            /** skip any action if no free spaces */
            if (!freeSpacesToGo.isEmpty()) {
                /** at this point the parking repositioning problem is solved */
                /** creating unitsToMove map */
                Map<Link, Integer> unitsToMove = RedistributionProblemHelper.getFlow(taxisToGo);

                /** set up the flow problem and solve */
                RedistributionProblemSolver<Link> parkingLP = //
                        new RedistributionProblemSolver<Link>(unitsToMove, freeSpacesToGo, //
                                (l1, l2) -> distanceFunction.getDistance(l1, l2), l -> l.getId().toString(), //
                                false, "");
                Map<Link, Map<Link, Integer>> flowSolution = parkingLP.returnSolution();

                /** compute command map */
                return RedistributionProblemHelper.getSolutionCommands(taxisToGo, flowSolution);
            }
        }
        return new HashMap<>();
    }

}
