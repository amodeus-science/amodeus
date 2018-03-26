package ch.ethz.idsc.amodeus.dispatcher;

import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.PartitionedDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractVehicleDestMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractVirtualNodeDest;
import ch.ethz.idsc.amodeus.dispatcher.util.BipartiteMatchingUtils;
import ch.ethz.idsc.amodeus.dispatcher.util.EuclideanDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.FeasibleRebalanceCreator;
import ch.ethz.idsc.amodeus.dispatcher.util.HungarBiPartVehicleDestMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.RandomVirtualNodeDest;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualLink;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNode;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.red.Total;
import ch.ethz.idsc.tensor.sca.Floor;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;

/** Implementation of the "Feedforward Fluidic Optimal Rebalancing Policy" presented in
 * Pavone, M., Smith, S.L., Frazzoli, E. and Rus, D., 2012. Robotic load balancing for mobility-on-demand systems.
 * The International Journal of Robotics
 * Research, 31(7), pp.839-854. */
public class FeedforwardFluidicRebalancingPolicy extends PartitionedDispatcher {
    public final int dispatchPeriod;
    public final int rebalancingPeriod;
    final AbstractVirtualNodeDest virtualNodeDest;
    final AbstractVehicleDestMatcher vehicleDestMatcher;
    private int total_rebalanceCount = 0;
    private final int nVNodes;
    private final int nVLinks;
    private final Network network;
    Tensor printVals = Tensors.empty();
    TravelData travelData;
    Tensor rebalancingRate;
    Tensor rebalanceCount;
    Tensor rebalanceCountInteger;

    public FeedforwardFluidicRebalancingPolicy( //
            Config config, //
            AVDispatcherConfig avconfig, //
            AVGeneratorConfig generatorConfig, //
            TravelTime travelTime, //
            ParallelLeastCostPathCalculator router, //
            EventsManager eventsManager, //
            Network network, //
            VirtualNetwork<Link> virtualNetwork, //
            AbstractVirtualNodeDest abstractVirtualNodeDest, //
            AbstractVehicleDestMatcher abstractVehicleDestMatcher, //
            TravelData travelData) {
        super(config, avconfig, travelTime, router, eventsManager, virtualNetwork);
        virtualNodeDest = abstractVirtualNodeDest;
        vehicleDestMatcher = abstractVehicleDestMatcher;
        this.travelData = travelData;
        this.network = network;
        nVNodes = virtualNetwork.getvNodesCount();
        nVLinks = virtualNetwork.getvLinksCount();
        rebalanceCount = Array.zeros(nVNodes, nVNodes);
        rebalanceCountInteger = Array.zeros(nVNodes, nVNodes);
        SafeConfig safeConfig = SafeConfig.wrap(avconfig);
        dispatchPeriod = safeConfig.getInteger("dispatchPeriod", 30);
        rebalancingPeriod = safeConfig.getInteger("rebalancingPeriod", 30);
    }

    @Override
    public void redispatch(double now) {
        final long round_now = Math.round(now);

        // Part I: permanently rebalance vehicles according to the rates output
        // by the LP
        if (round_now % rebalancingPeriod == 0) {
            rebalancingRate = travelData.getAlphaijPSFforTime((int) round_now);

            // update rebalance count using current rate
            rebalanceCount = rebalanceCount.add(rebalancingRate.multiply(RealScalar.of(rebalancingPeriod)));
            rebalanceCountInteger = Floor.of(rebalanceCount);
            rebalanceCount = rebalanceCount.subtract(rebalanceCountInteger);

            // ensure that not more vehicles are sent away than available
            Map<VirtualNode<Link>, List<RoboTaxi>> availableVehicles = getVirtualNodeDivertableNotRebalancingRoboTaxis();
            Tensor feasibleRebalanceCount = FeasibleRebalanceCreator.returnFeasibleRebalance(rebalanceCountInteger.unmodifiable(), availableVehicles);
            total_rebalanceCount += (Integer) ((Scalar) Total.of(Tensor.of(feasibleRebalanceCount.flatten(-1)))).number();

            // generate routing instructions for rebalancing vehicles
            Map<VirtualNode<Link>, List<Link>> destinationLinks = virtualNetwork.createVNodeTypeMap();

            // fill rebalancing destinations
            for (int i = 0; i < nVLinks; ++i) {
                VirtualLink<Link> virtualLink = this.virtualNetwork.getVirtualLink(i);
                VirtualNode<Link> toNode = virtualLink.getTo();
                VirtualNode<Link> fromNode = virtualLink.getFrom();
                int numreb = (Integer) (feasibleRebalanceCount.Get(fromNode.getIndex(), toNode.getIndex())).number();
                List<Link> rebalanceTargets = virtualNodeDest.selectLinkSet(toNode, numreb);
                destinationLinks.get(fromNode).addAll(rebalanceTargets);
            }

            // consistency check: rebalancing destination links must not exceed
            // available vehicles
            // in virtual node
            GlobalAssert.that(!virtualNetwork.getVirtualNodes().stream().filter(v -> availableVehicles.get(v).size() < destinationLinks.get(v).size()).findAny().isPresent());

            // send rebalancing vehicles using the setVehicleRebalance command
            for (VirtualNode<Link> virtualNode : destinationLinks.keySet()) {
                Map<RoboTaxi, Link> rebalanceMatching = vehicleDestMatcher.matchLink(availableVehicles.get(virtualNode), destinationLinks.get(virtualNode));
                rebalanceMatching.keySet().forEach(v -> setRoboTaxiRebalance(v, rebalanceMatching.get(v)));
            }

            // reset vector
            rebalanceCountInteger = Array.zeros(nVNodes, nVNodes);
        }

        // Part II: outside rebalancing periods, permanently assign destinations
        // to vehicles using
        // bipartite matching
        if (round_now % dispatchPeriod == 0) {
            printVals = BipartiteMatchingUtils.executePickup(this::setRoboTaxiPickup, getDivertableRoboTaxis(), getAVRequests(), //
                    new EuclideanDistanceFunction(), network, false);
        }
    }

    @Override
    protected String getInfoLine() {
        return String.format("%s RV=%s H=%s", //
                super.getInfoLine(), //
                total_rebalanceCount, //
                printVals.toString() //
        );
    }

    public static class Factory implements AVDispatcherFactory {
        @Inject
        @Named(AVModule.AV_MODE)
        private ParallelLeastCostPathCalculator router;

        @Inject
        @Named(AVModule.AV_MODE)
        private TravelTime travelTime;

        @Inject
        private EventsManager eventsManager;

        @Inject
        @Named(AVModule.AV_MODE)
        private Network network;

        @Inject(optional = true)
        private VirtualNetwork<Link> virtualNetwork;

        @Inject(optional = true)
        private TravelData travelData;

        @Inject
        private Config config;

        @Override
        public AVDispatcher createDispatcher(AVDispatcherConfig avconfig) {
            AVGeneratorConfig generatorConfig = avconfig.getParent().getGeneratorConfig();

            AbstractVirtualNodeDest abstractVirtualNodeDest = new RandomVirtualNodeDest();
            AbstractVehicleDestMatcher abstractVehicleDestMatcher = new HungarBiPartVehicleDestMatcher(new EuclideanDistanceFunction());

            return new FeedforwardFluidicRebalancingPolicy(config, avconfig, generatorConfig, travelTime, router, eventsManager, network, virtualNetwork, abstractVirtualNodeDest,
                    abstractVehicleDestMatcher, travelData);
        }
    }
}