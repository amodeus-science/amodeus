/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

/** All dispatchers wich perform rebalancing and use a virtualNetwork dividing the city into zones are derived from {@link PartitionedDispatcher}.
 * A {@link PartitionedDispatcher} always has a {@link VirtualNetwork} */
public abstract class SharedPartitionedDispatcher extends SharedRebalancingDispatcher {
    protected final VirtualNetwork<Link> virtualNetwork; //

    protected SharedPartitionedDispatcher( //
            Config config, //
            AVDispatcherConfig avconfig, //
            TravelTime travelTime, //
            ParallelLeastCostPathCalculator router, //
            EventsManager eventsManager, //
            VirtualNetwork<Link> virtualNetwork, //
            MatsimAmodeusDatabase db) {
        super(config, avconfig, travelTime, router, eventsManager, db);

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
}
