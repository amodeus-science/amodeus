/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
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

import ch.ethz.idsc.amodeus.dispatcher.core.DispatcherConfig;
import ch.ethz.idsc.amodeus.dispatcher.core.PartitionedDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractRoboTaxiDestMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractVirtualNodeDest;
import ch.ethz.idsc.amodeus.dispatcher.util.BipartiteMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.ConfigurableBipartiteMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceCost;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceHeuristics;
import ch.ethz.idsc.amodeus.dispatcher.util.EuclideanDistanceCost;
import ch.ethz.idsc.amodeus.dispatcher.util.FeasibleRebalanceCreator;
import ch.ethz.idsc.amodeus.dispatcher.util.GlobalBipartiteMatching;
import ch.ethz.idsc.amodeus.dispatcher.util.RandomVirtualNodeDest;
import ch.ethz.idsc.amodeus.lp.LPCreator;
import ch.ethz.idsc.amodeus.lp.LPTimeInvariant;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualLink;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
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
import ch.ethz.matsim.av.router.AVRouter;

/** Implementation of the "Feedforward Fluidic Optimal Rebalancing Policy" presented in
 * Pavone, M., Smith, S.L., Frazzoli, E. and Rus, D., 2012.
 * Robotic load balancing for mobility-on-demand systems.
 * The International Journal of Robotics Research, 31(7), pp.839-854. */
public class FeedforwardFluidicRebalancingPolicy extends PartitionedDispatcher {
    private final AbstractVirtualNodeDest virtualNodeDest;
    private final AbstractRoboTaxiDestMatcher vehicleDestMatcher;
    private final Network network;
    private final TravelData travelData;
    private final DistanceFunction distanceFunction;
    private final DistanceHeuristics distanceHeuristics;
    private final BipartiteMatcher bipartiteMatcher;
    private final int dispatchPeriod;
    private final int rebalancingPeriod;
    private final int nVNodes;
    private final int nVLinks;
    // ---
    private int total_rebalanceCount = 0;
    private boolean started = false;
    private Tensor printVals = Tensors.empty();
    private Tensor rebalancingRate;
    private Tensor rebalanceCount;
    private Tensor rebalanceCountInteger;

    public FeedforwardFluidicRebalancingPolicy( //
            Config config, //
            AVDispatcherConfig avDispatcherConfig, //
            AVGeneratorConfig generatorConfig, //
            TravelTime travelTime, //
            AVRouter router, //
            EventsManager eventsManager, //
            Network network, //
            VirtualNetwork<Link> virtualNetwork, //
            AbstractVirtualNodeDest abstractVirtualNodeDest, //
            AbstractRoboTaxiDestMatcher abstractVehicleDestMatcher, //
            TravelData travelData, //
            MatsimAmodeusDatabase db) {
        super(config, avDispatcherConfig, travelTime, router, eventsManager, virtualNetwork, db);
        virtualNodeDest = abstractVirtualNodeDest;
        vehicleDestMatcher = abstractVehicleDestMatcher;

        this.network = network;
        nVNodes = virtualNetwork.getvNodesCount();
        nVLinks = virtualNetwork.getvLinksCount();
        rebalanceCount = Array.zeros(nVNodes, nVNodes);
        rebalanceCountInteger = Array.zeros(nVNodes, nVNodes);
        DispatcherConfig dispatcherConfig = DispatcherConfig.wrap(avDispatcherConfig);
        dispatchPeriod = dispatcherConfig.getDispatchPeriod(30);
        rebalancingPeriod = dispatcherConfig.getRebalancingPeriod(30);
        distanceHeuristics = dispatcherConfig.getDistanceHeuristics(DistanceHeuristics.EUCLIDEAN);
        System.out.println("Using DistanceHeuristics: " + distanceHeuristics.name());
        this.distanceFunction = distanceHeuristics.getDistanceFunction(network);
        this.bipartiteMatcher = new ConfigurableBipartiteMatcher(network, new DistanceCost(distanceFunction), //
                SafeConfig.wrap(avDispatcherConfig));
        this.travelData = travelData;
        System.out.println(travelData.getLPName());
        System.out.println(LPTimeInvariant.class.getSimpleName());
        if (!travelData.getLPName().equals(LPTimeInvariant.class.getSimpleName())) {
            System.err.println("Running the " + this.getClass().getSimpleName() + " requires precomputed data that must be\n"
                    + "computed in the ScenarioPreparer. Currently the file LPOptions.properties is set to compute the feedforward\n" + "rebalancing data with: ");
            System.err.println(travelData.getLPName());
            System.err.println("The correct setting in LPOptions.properties to run this dispatcher is:  " + LPCreator.TIMEINVARIANT.name());
            GlobalAssert.that(false);
        }
    }

    @Override
    public void redispatch(double now) {
        long round_now = Math.round(now);

        if (!started) {
            if (getRoboTaxis().size() == 0) /** return if the roboTaxis are not ready yet */
                return;
            /** as soon as the roboTaxis are ready, make sure to execute rebalancing and dispatching for now=0 */
            round_now = 0;
            started = true;
        }

        /** Part I: permanently rebalance vehicles according to the rates output by the LP */
        if (round_now % rebalancingPeriod == 0 && travelData.coversTime(round_now)) {
            rebalancingRate = travelData.getAlphaRateAtTime((int) round_now);

            /** update rebalance count using current rate */
            rebalanceCount = rebalanceCount.add(rebalancingRate.multiply(RealScalar.of(rebalancingPeriod)));
            rebalanceCountInteger = Floor.of(rebalanceCount);
            rebalanceCount = rebalanceCount.subtract(rebalanceCountInteger);

            /** ensure that not more vehicles are sent away than available */
            Map<VirtualNode<Link>, List<RoboTaxi>> availableVehicles = getVirtualNodeDivertableNotRebalancingRoboTaxis();
            Tensor feasibleRebalanceCount = FeasibleRebalanceCreator.returnFeasibleRebalance(rebalanceCountInteger.unmodifiable(), availableVehicles);
            total_rebalanceCount += (Integer) ((Scalar) Total.of(Tensor.of(feasibleRebalanceCount.flatten(-1)))).number();

            /** generate routing instructions for rebalancing vehicles */
            Map<VirtualNode<Link>, List<Link>> destinationLinks = virtualNetwork.createVNodeTypeMap();

            /** fill rebalancing destinations */
            for (int i = 0; i < nVLinks; ++i) {
                VirtualLink<Link> virtualLink = this.virtualNetwork.getVirtualLink(i);
                VirtualNode<Link> toNode = virtualLink.getTo();
                VirtualNode<Link> fromNode = virtualLink.getFrom();
                int numreb = (Integer) (feasibleRebalanceCount.Get(fromNode.getIndex(), toNode.getIndex())).number();
                List<Link> rebalanceTargets = virtualNodeDest.selectLinkSet(toNode, numreb);
                destinationLinks.get(fromNode).addAll(rebalanceTargets);
            }

            /** consistency check: rebalancing destination links must not exceed available vehicles in virtual node */
            GlobalAssert.that(!virtualNetwork.getVirtualNodes().stream().filter(v -> availableVehicles.get(v).size() < destinationLinks.get(v).size()).findAny().isPresent());

            /** send rebalancing vehicles using the setVehicleRebalance command */
            for (VirtualNode<Link> virtualNode : destinationLinks.keySet()) {
                Map<RoboTaxi, Link> rebalanceMatching = vehicleDestMatcher.matchLink(availableVehicles.get(virtualNode), destinationLinks.get(virtualNode));
                rebalanceMatching.keySet().forEach(v -> setRoboTaxiRebalance(v, rebalanceMatching.get(v)));
            }

            rebalanceCountInteger = Array.zeros(nVNodes, nVNodes);
        }

        /** Part II: outside rebalancing periods, permanently assign destinations to vehicles using
         * bipartite matching */
        if (round_now % dispatchPeriod == 0) {
            printVals = bipartiteMatcher.executePickup(this, getDivertableRoboTaxis(), //
                    getAVRequests(), distanceFunction, network);
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

        @Inject
        private MatsimAmodeusDatabase db;

        @Override
        public AVDispatcher createDispatcher(AVDispatcherConfig avconfig, AVRouter router) {
            AVGeneratorConfig generatorConfig = avconfig.getParent().getGeneratorConfig();
            AbstractVirtualNodeDest abstractVirtualNodeDest = new RandomVirtualNodeDest();
            AbstractRoboTaxiDestMatcher abstractVehicleDestMatcher = new GlobalBipartiteMatching(EuclideanDistanceCost.INSTANCE);
            return new FeedforwardFluidicRebalancingPolicy(config, avconfig, generatorConfig, travelTime, router, eventsManager, network, virtualNetwork, abstractVirtualNodeDest,
                    abstractVehicleDestMatcher, travelData, db);
        }
    }
}