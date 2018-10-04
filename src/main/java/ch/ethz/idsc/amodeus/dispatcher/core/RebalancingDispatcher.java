/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.List;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.net.MatsimStaticDatabase;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

/** class for wich all Dispatchers performing rebalancing, i.e., replacement of empty vehicles should be derived */
public abstract class RebalancingDispatcher extends UniversalDispatcher {

    protected RebalancingDispatcher(Config config, AVDispatcherConfig avDispatcherConfig, TravelTime travelTime, //
            ParallelLeastCostPathCalculator parallelLeastCostPathCalculator, EventsManager eventsManager, //
            MatsimStaticDatabase db) {
        super(config, avDispatcherConfig, travelTime, parallelLeastCostPathCalculator, eventsManager, db);
    }

    /** Command to rebalance {@link RoboTaxi} to a certain {@link Link} destination. The {@link RoboTaxi} will appear as
     * Rebalancing in the visualizer. Can only be used for {@link RoboTaxi} which are without a customer and divertible.
     * Function can only be invoked one time in each iteration of {@link VehicleMainatainer.redispatch}
     * 
     * @param roboTaxi
     * @param destination */
    public final void setRoboTaxiRebalance(final RoboTaxi roboTaxi, final Link destination) {
        GlobalAssert.that(roboTaxi.isWithoutCustomer());
        /** if {@link RoboTaxi} is during pickup, remove from pickup register */
        if (isInPickupRegister(roboTaxi)) {
            AVRequest toRemove = getPickupRoboTaxis().get(roboTaxi);
            removeFromPickupRegisters(toRemove);
        }
        setRoboTaxiDiversion(roboTaxi, destination, RoboTaxiStatus.REBALANCEDRIVE);
        eventsManager.processEvent(RebalanceVehicleEvent.create(getTimeNow(), roboTaxi, destination));
    }

    /** @return {@link java.util.List } of all {@link RoboTaxi} which are currently rebalancing. */
    protected List<RoboTaxi> getRebalancingRoboTaxis() {
        return getRoboTaxis().stream()//
                .filter(rt -> rt.getStatus().equals(RoboTaxiStatus.REBALANCEDRIVE))//
                .collect(Collectors.toList());
    }

    /** @return {@link java.util.List} of all {@link RoboTaxi} which are divertable and not in a rebalacing
     *         task. */
    protected List<RoboTaxi> getDivertableNotRebalancingRoboTaxis() {
        return getDivertableRoboTaxis().stream()//
                .filter(rt -> !rt.getStatus().equals(RoboTaxiStatus.REBALANCEDRIVE))//
                .collect(Collectors.toList());
    }

}
