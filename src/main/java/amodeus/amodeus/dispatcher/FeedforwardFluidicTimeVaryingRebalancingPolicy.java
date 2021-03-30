/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher;

import java.util.List;
import java.util.Map;

import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.TypeLiteral;

import amodeus.amodeus.dispatcher.core.DispatcherConfigWrapper;
import amodeus.amodeus.dispatcher.core.PartitionedDispatcher;
import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.RoboTaxiUsageType;
import amodeus.amodeus.dispatcher.util.AbstractRoboTaxiDestMatcher;
import amodeus.amodeus.dispatcher.util.AbstractVirtualNodeDest;
import amodeus.amodeus.dispatcher.util.BipartiteMatcher;
import amodeus.amodeus.dispatcher.util.ConfigurableBipartiteMatcher;
import amodeus.amodeus.dispatcher.util.DistanceCost;
import amodeus.amodeus.dispatcher.util.DistanceHeuristics;
import amodeus.amodeus.dispatcher.util.EuclideanDistanceCost;
import amodeus.amodeus.dispatcher.util.FeasibleRebalanceCreator;
import amodeus.amodeus.dispatcher.util.GlobalBipartiteMatching;
import amodeus.amodeus.dispatcher.util.RandomVirtualNodeDest;
import amodeus.amodeus.generator.VehicleToVSGenerator;
import amodeus.amodeus.lp.LPCreator;
import amodeus.amodeus.lp.LPTimeVariant;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.routing.DistanceFunction;
import amodeus.amodeus.traveldata.TravelData;
import amodeus.amodeus.util.math.GlobalAssert;
import amodeus.amodeus.util.math.Scalar2Number;
import amodeus.amodeus.util.math.TotalAll;
import amodeus.amodeus.util.matsim.SafeConfig;
import amodeus.amodeus.virtualnetwork.core.VirtualLink;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;
import amodeus.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.sca.Floor;

/** Implementation of the feedforward time-varying rebalancing policy presented in
 * Spieser, Kevin, Samitha Samaranayake, and Emilio Frazzoli.
 * "Vehicle routing for shared-mobility systems with time-varying demand."
 * American Control Conference (ACC), 2016. IEEE, 2016. */
public class FeedforwardFluidicTimeVaryingRebalancingPolicy extends PartitionedDispatcher {
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

    public FeedforwardFluidicTimeVaryingRebalancingPolicy( //
            Config config, AmodeusModeConfig operatorConfig, //
            TravelTime travelTime, AmodeusRouter router, //
            EventsManager eventsManager, Network network, VirtualNetwork<Link> virtualNetwork, //
            AbstractVirtualNodeDest abstractVirtualNodeDest, //
            AbstractRoboTaxiDestMatcher abstractVehicleDestMatcher, TravelData travelData, //
            MatsimAmodeusDatabase db, RebalancingStrategy rebalancingStrategy) {
        super(config, operatorConfig, travelTime, router, eventsManager, virtualNetwork, db, rebalancingStrategy, RoboTaxiUsageType.SINGLEUSED);
        virtualNodeDest = abstractVirtualNodeDest;
        vehicleDestMatcher = abstractVehicleDestMatcher;
        this.network = network;
        this.travelData = travelData;
        nVNodes = virtualNetwork.getvNodesCount();
        nVLinks = virtualNetwork.getvLinksCount();
        rebalanceCount = Array.zeros(nVNodes, nVNodes);
        rebalanceCountInteger = Array.zeros(nVNodes, nVNodes);
        DispatcherConfigWrapper dispatcherConfig = DispatcherConfigWrapper.wrap(operatorConfig.getDispatcherConfig());
        dispatchPeriod = dispatcherConfig.getDispatchPeriod(30);
        rebalancingPeriod = dispatcherConfig.getRebalancingPeriod(30);
        distanceHeuristics = dispatcherConfig.getDistanceHeuristics(DistanceHeuristics.EUCLIDEAN);
        System.out.println("Using DistanceHeuristics: " + distanceHeuristics.name());
        this.distanceFunction = distanceHeuristics.getDistanceFunction(network);
        this.bipartiteMatcher = new ConfigurableBipartiteMatcher(network, new DistanceCost(distanceFunction), //
                SafeConfig.wrap(operatorConfig.getDispatcherConfig()));
        if (!travelData.getLPName().equals(LPTimeVariant.class.getSimpleName())) {
            System.err.println("Running the " + this.getClass().getSimpleName() + " requires precomputed data that must be\n"
                    + "computed in the ScenarioPreparer. Currently the file LPOptions.properties is set to compute the feedforward\n" + "rebalcing data with: ");
            System.err.println(travelData.getLPName());
            System.err.println("The correct setting in LPOptions.properties to run this dispatcher is:  " + LPCreator.TIMEVARIANT.name());
            GlobalAssert.that(false);
        }
        GlobalAssert.that(operatorConfig.getGeneratorConfig().getType().equals(VehicleToVSGenerator.class.getSimpleName()));
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

            total_rebalanceCount += (Integer) Scalar2Number.of(TotalAll.of(feasibleRebalanceCount));

            /** generate routing instructions for rebalancing vehicles */
            Map<VirtualNode<Link>, List<Link>> destinationLinks = virtualNetwork.createVNodeTypeMap();

            /** fill rebalancing destinations */
            for (int i = 0; i < nVLinks; ++i) {
                VirtualLink<Link> virtualLink = this.virtualNetwork.getVirtualLink(i);
                VirtualNode<Link> toNode = virtualLink.getTo();
                VirtualNode<Link> fromNode = virtualLink.getFrom();
                int numreb = (Integer) Scalar2Number.of(feasibleRebalanceCount.Get(fromNode.getIndex(), toNode.getIndex()));
                List<Link> rebalanceTargets = virtualNodeDest.selectLinkSet(toNode, numreb);
                destinationLinks.get(fromNode).addAll(rebalanceTargets);
            }

            /** consistency check: rebalancing destination links must not exceed available vehicles in virtual node */
            GlobalAssert.that(virtualNetwork.getVirtualNodes().stream().noneMatch(v -> availableVehicles.get(v).size() < destinationLinks.get(v).size()));

            /** send rebalancing vehicles using the setVehicleRebalance command */
            for (VirtualNode<Link> virtualNode : destinationLinks.keySet()) {
                Map<RoboTaxi, Link> rebalanceMatching = vehicleDestMatcher.matchLink(availableVehicles.get(virtualNode), destinationLinks.get(virtualNode));
                rebalanceMatching.keySet().forEach(v -> setRoboTaxiRebalance(v, rebalanceMatching.get(v)));
            }

            rebalanceCountInteger = Array.zeros(nVNodes, nVNodes);
        }

        /** Part II: outside rebalancing periods, permanently assign destinations to vehicles using
         * bipartite matching */
        if (round_now % dispatchPeriod == 0)
            printVals = bipartiteMatcher.executePickup(this, getDivertableRoboTaxis(), //
                    getPassengerRequests(), distanceFunction, network);
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
        @Override
        public AmodeusDispatcher createDispatcher(InstanceGetter inject) {
            Config config = inject.get(Config.class);
            MatsimAmodeusDatabase db = inject.get(MatsimAmodeusDatabase.class);
            EventsManager eventsManager = inject.get(EventsManager.class);

            AmodeusModeConfig operatorConfig = inject.getModal(AmodeusModeConfig.class);
            Network network = inject.getModal(Network.class);
            AmodeusRouter router = inject.getModal(AmodeusRouter.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);

            VirtualNetwork<Link> virtualNetwork = inject.getModal(new TypeLiteral<VirtualNetwork<Link>>() {
            });

            TravelData travelData = inject.getModal(TravelData.class);
            RebalancingStrategy rebalancingStrategy = inject.getModal(RebalancingStrategy.class);

            AbstractVirtualNodeDest abstractVirtualNodeDest = new RandomVirtualNodeDest();
            AbstractRoboTaxiDestMatcher abstractVehicleDestMatcher = new GlobalBipartiteMatching(EuclideanDistanceCost.INSTANCE);
            return new FeedforwardFluidicTimeVaryingRebalancingPolicy(config, operatorConfig, travelTime, router, eventsManager, network, virtualNetwork, abstractVirtualNodeDest,
                    abstractVehicleDestMatcher, travelData, db, rebalancingStrategy);
        }
    }
}