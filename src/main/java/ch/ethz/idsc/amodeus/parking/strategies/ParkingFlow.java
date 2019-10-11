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
import ch.ethz.idsc.amodeus.lp.RedistributionProblemHelper;
import ch.ethz.idsc.amodeus.lp.RedistributionProblemSolver;
import ch.ethz.idsc.amodeus.parking.capacities.ParkingCapacity;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

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
                HashMap<Link, Integer> unitsToMove = RedistributionProblemHelper.getFlow(taxisToGo);

                /** if not enough free spaces area available, shorten the units to send
                 * by an equal number in all overflowing parking reservoirs */
                int totalUnits = unitsToMove.values().stream().mapToInt(i -> i).sum();
                int totalSpots = freeSpacesToGo.values().stream().mapToInt(i -> i).sum();

                /** if more units to be moved than spots available, reduce size */
                while (totalUnits > totalSpots) {
                    EqualReduction.apply(unitsToMove, totalSpots);
                    totalUnits = unitsToMove.values().stream().mapToInt(i -> i).sum();
                }

                // remove elements with value 0 to speed up computation of LP
                unitsToMove = StaticHelper.removeZeroValues(unitsToMove);

                freeSpacesToGo.entrySet().forEach(e -> {
                    if (e.getValue() == 0) {
                        System.err.println("Has available dest with 0...");
                    }
                });

                GlobalAssert.that(totalUnits <= totalSpots);

                /** if there are less parking spots than vehicles, the total units to displace
                 * may be zero and the LP does not need to be solved. */
                if (totalUnits > 0) {
                    System.out.println("1, totalUnits: " + totalUnits);
                    SmallRedistributionProblemSolver<Link> fastSolver = null;
                    RedistributionProblemSolver<Link> parkingLP = null;
                    Map<Link, Map<Link, Integer>> flowSolution = null;
                    boolean foundSolution = false;
                    /** attempt to solve without a linear program for small numbers
                     * of units to move */
                    if (totalUnits < 20) { // TODO find meaningful value, remove magic const.
                        System.out.println("Fast solver is started...");
                        fastSolver = new SmallRedistributionProblemSolver<>(unitsToMove, freeSpacesToGo, //
                                (l1, l2) -> distanceFunction.getDistance(l1, l2), l -> l.getId().toString(), //
                                false, "");
                        foundSolution = fastSolver.success();
                        System.out.println("Fast solver status: " + foundSolution);

                        if (totalUnits == 1 && !fastSolver.success()) {
                            GlobalAssert.that(false);
                        }

                        flowSolution = fastSolver.returnSolution();
                    }
                    /** otherwise, setup and solve LP to find solution */
                    if (!foundSolution) {
                        /** set up the flow problem and solve */
                        parkingLP = new RedistributionProblemSolver<>(unitsToMove, freeSpacesToGo, //
                                (l1, l2) -> distanceFunction.getDistance(l1, l2), l -> l.getId().toString(), //
                                false, "");
                        flowSolution = parkingLP.returnSolution();
                    }
                    /** compute command map */
                    return RedistributionProblemHelper.getSolutionCommands(taxisToGo, flowSolution);
                }
            }
        }
        return new HashMap<>();
    }

}
