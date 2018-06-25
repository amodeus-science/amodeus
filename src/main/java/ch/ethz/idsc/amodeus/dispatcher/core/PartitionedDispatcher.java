/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNode;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

/** All dispatchers wich perform rebalancing and use a virtualNetwork dividing the city into zones are derived from {@link PartitionedDispatcher}.
 * A {@link PartitionedDispatcher} always has a {@link VirtualNetwork}
 * 
 * @author Claudio Ruch
 * @param <T> */
public abstract class PartitionedDispatcher extends RebalancingDispatcher {
    protected final VirtualNetwork<Link> virtualNetwork; //

    protected PartitionedDispatcher( //
            Config config, //
            AVDispatcherConfig avconfig, //
            TravelTime travelTime, //
            ParallelLeastCostPathCalculator router, //
            EventsManager eventsManager, //
            VirtualNetwork<Link> virtualNetwork) {
        super(config, avconfig, travelTime, router, eventsManager);

        if (virtualNetwork == null) {
            throw new IllegalStateException(
                    "The VirtualNetwork is not set. Make sure you active DefaultVirtualNetworkModule in the ScenarioServer, OR provide a custom VirtualNetwork via injection.");
        }

        this.virtualNetwork = Objects.requireNonNull(virtualNetwork);
    }

    /** @return {@link java.util.Map} where all {@link AVRequest} are listed at the {@link VirtualNode} where their {@link AVRequest.fromLink} is. */
    protected Map<VirtualNode<Link>, List<AVRequest>> getVirtualNodeRequests() {
        return virtualNetwork.binToVirtualNode(getAVRequests(), AVRequest::getFromLink);
    }

    /** @return {@link java.util.Map} where all divertable not rebalancing {@link UnitCapRoboTaxi} are listed at the {@link VirtualNode} where their {@link Link}
     *         divertableLocation is. */
    protected Map<VirtualNode<Link>, List<UnitCapRoboTaxi>> getVirtualNodeDivertableNotRebalancingRoboTaxis() {
        return virtualNetwork.binToVirtualNode(getDivertableNotRebalancingRoboTaxis(), UnitCapRoboTaxi::getDivertableLocation);
    }

    /** @return {@link java.util.Map} where all rebalancing {@link UnitCapRoboTaxi} are listed at the {@link VirtualNode} where their {@link Link} current
     *         driveDestination is. */
    protected Map<VirtualNode<Link>, List<UnitCapRoboTaxi>> getVirtualNodeRebalancingToRoboTaxis() {
        return virtualNetwork.binToVirtualNode(getRebalancingRoboTaxis(), UnitCapRoboTaxi::getCurrentDriveDestination);
    }

    /** @return {@link java.util.Map} where all roboTaxis with customer {@link UnitCapRoboTaxi} are listed at the {@link VirtualNode} where their {@link Link} current
     *         driveDestination is. */
    protected Map<VirtualNode<Link>, List<UnitCapRoboTaxi>> getVirtualNodeArrivingWithCustomerRoboTaxis() {
        return virtualNetwork.binToVirtualNode(getRoboTaxiSubset(RoboTaxiStatus.DRIVEWITHCUSTOMER), UnitCapRoboTaxi::getCurrentDriveDestination);
    }

    /** @return {@link java.util.Map} where all stay roboTaxis with customer {@link UnitCapRoboTaxi} are listed at the {@link VirtualNode} where their {@link Link}
     *         current
     *         divertableLocation is. */
    protected Map<VirtualNode<Link>, List<UnitCapRoboTaxi>> getVirtualNodeStayVehicles() {
        return virtualNetwork.binToVirtualNode(getRoboTaxiSubset(RoboTaxiStatus.STAY), UnitCapRoboTaxi::getDivertableLocation);
    }

}
