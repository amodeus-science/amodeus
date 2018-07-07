/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.List;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

/** class for wich all Dispatchers performing rebalancing, i.e., replacement of empty vehicles should be derived */
/** @author Nicolo Ormezzano, Lukas Sieber */
public abstract class SharedRebalancingDispatcher extends SharedUniversalDispatcher {

    protected SharedRebalancingDispatcher(Config config, AVDispatcherConfig avDispatcherConfig, TravelTime travelTime,
            ParallelLeastCostPathCalculator parallelLeastCostPathCalculator, EventsManager eventsManager) {
        super(config, avDispatcherConfig, travelTime, parallelLeastCostPathCalculator, eventsManager);
    }

    /** Command to rebalance {@link RoboTaxi} to a certain {@link Link} destination. The {@link RoboTaxi} appears as
     * Rebalancing in the visualizer afterwards. Can only be used for {@link RoboTaxi} which are without a customer.
     * Function can only be invoked one time in each iteration of {@link VehicleMainatainer.redispatch}
     * 
     * @param roboTaxi
     * @param destination */
    protected final void setRoboTaxiRebalance(final RoboTaxi roboTaxi, final Link destination) {
        GlobalAssert.that(roboTaxi.isWithoutCustomer());
        // Clear menu and put requests back to pending requests taking them out from request and pickup register.
        cleanRoboTaxiMenuAndAbandonAssignedRequests(roboTaxi);
        GlobalAssert.that(!roboTaxi.getMenu().hasStarter());
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
