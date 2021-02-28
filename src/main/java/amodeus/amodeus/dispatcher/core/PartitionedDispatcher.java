/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.core;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.plpc.ParallelLeastCostPathCalculator;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;
import amodeus.amodeus.virtualnetwork.core.VirtualNode;

/** All dispatchers wich perform rebalancing and use a virtualNetwork dividing the city into zones are derived from {@link PartitionedDispatcher}.
 * A {@link PartitionedDispatcher} always has a {@link VirtualNetwork} */
public abstract class PartitionedDispatcher extends RebalancingDispatcher {
    protected final VirtualNetwork<Link> virtualNetwork; //

    protected PartitionedDispatcher( //
            Config config, //
            AmodeusModeConfig operatorConfig, //
            TravelTime travelTime, //
            ParallelLeastCostPathCalculator router, //
            EventsManager eventsManager, //
            VirtualNetwork<Link> virtualNetwork, //
            MatsimAmodeusDatabase db, RebalancingStrategy rebalancingStrategy, RoboTaxiUsageType usageType) {
        super(config, operatorConfig, travelTime, router, eventsManager, db, rebalancingStrategy, usageType);

        if (virtualNetwork == null)
            throw new IllegalStateException(
                    "The VirtualNetwork is not set. Make sure you active DefaultVirtualNetworkModule in the ScenarioServer, OR provide a custom VirtualNetwork via injection.");

        this.virtualNetwork = Objects.requireNonNull(virtualNetwork);
    }

    /** @return {@link java.util.Map} where all {@link PassengerRequest} are listed at the {@link VirtualNode} where their {@link PassengerRequest.fromLink} is. */
    protected Map<VirtualNode<Link>, List<PassengerRequest>> getVirtualNodeRequests() {
        return virtualNetwork.binToVirtualNode(getPassengerRequests(), PassengerRequest::getFromLink);
    }

    /** @return {@link java.util.Map} where all {@link PassengerRequest} are listed at the {@link VirtualNode} where their {@link PassengerRequest.fromLink} is. */
    protected Map<VirtualNode<Link>, List<PassengerRequest>> getVirtualNodeUnassignedRequests() {
        return getVirtualNodeRequests();
    }

    /** @return {@link java.util.Map} where all divertable not rebalancing {@link RoboTaxi} are listed at the {@link VirtualNode} where their {@link Link}
     *         divertableLocation is. */
    protected Map<VirtualNode<Link>, List<RoboTaxi>> getVirtualNodeDivertableNotRebalancingRoboTaxis() {
        return virtualNetwork.binToVirtualNode(getDivertableNotRebalancingRoboTaxis(), RoboTaxi::getDivertableLocation);
    }

    /** @return {@link java.util.Map} where all rebalancing {@link RoboTaxi} are listed at the {@link VirtualNode} where their {@link Link} current
     *         driveDestination is. */
    protected Map<VirtualNode<Link>, List<RoboTaxi>> getVirtualNodeRebalancingToRoboTaxis() {
        return virtualNetwork.binToVirtualNode(getRebalancingRoboTaxis(), RoboTaxi::getCurrentDriveDestination);
    }

    /** @return {@link java.util.Map} where all roboTaxis with customer {@link RoboTaxi} are listed at the {@link VirtualNode} where their {@link Link}
     *         current
     *         driveDestination is. */
    protected Map<VirtualNode<Link>, List<RoboTaxi>> getVirtualNodeArrivingWithCustomerRoboTaxis() {
        return virtualNetwork.binToVirtualNode(getRoboTaxiSubset(RoboTaxiStatus.DRIVEWITHCUSTOMER), RoboTaxi::getCurrentDriveDestination);
    }

    /** @return {@link java.util.Map} where all stay roboTaxis with customer {@link RoboTaxi} are listed at the {@link VirtualNode} where their {@link Link}
     *         current
     *         divertableLocation is. */
    protected Map<VirtualNode<Link>, List<RoboTaxi>> getVirtualNodeStayVehicles() {
        return virtualNetwork.binToVirtualNode(getRoboTaxiSubset(RoboTaxiStatus.STAY), RoboTaxi::getDivertableLocation);
    }

    // new added by luc for congestion study
    /** @return {@link java.util.Map} where all roboTaxis with customer {@link RoboTaxi} are listed at the {@link VirtualNode} where their {@link Link}
     *         current
     *         divertableLocation is. */
    protected Map<VirtualNode<Link>, List<RoboTaxi>> getVirtualNodeDriveWithCustomerRoboTaxis() {
        return virtualNetwork.binToVirtualNode(getRoboTaxiSubset(RoboTaxiStatus.DRIVEWITHCUSTOMER), RoboTaxi::getDivertableLocation);
    }
}
