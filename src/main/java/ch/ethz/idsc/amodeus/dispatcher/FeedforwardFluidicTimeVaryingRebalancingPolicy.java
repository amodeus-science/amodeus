/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher;

import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.TypeLiteral;

import ch.ethz.idsc.amodeus.dispatcher.core.DispatcherConfigWrapper;
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
import ch.ethz.idsc.amodeus.lp.LPTimeVariant;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.matsim.mod.VehicleToVSGenerator;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.TotalAll;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualLink;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.sca.Floor;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.router.AVRouter;

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
            Config config, OperatorConfig operatorConfig, //
            TravelTime travelTime, AVRouter router, //
            EventsManager eventsManager, Network network, VirtualNetwork<Link> virtualNetwork, //
            AbstractVirtualNodeDest abstractVirtualNodeDest, //
            AbstractRoboTaxiDestMatcher abstractVehicleDestMatcher, TravelData travelData, //
            MatsimAmodeusDatabase db) {
        super(config, operatorConfig, travelTime, router, eventsManager, virtualNetwork, db);
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

            total_rebalanceCount += (Integer) TotalAll.of(feasibleRebalanceCount).number();

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
                    getAVRequests(), distanceFunction, network);
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
        public AVDispatcher createDispatcher(InstanceGetter inject) {
            Config config = inject.get(Config.class);
            MatsimAmodeusDatabase db = inject.get(MatsimAmodeusDatabase.class);
            EventsManager eventsManager = inject.get(EventsManager.class);

            OperatorConfig operatorConfig = inject.getModal(OperatorConfig.class);
            Network network = inject.getModal(Network.class);
            AVRouter router = inject.getModal(AVRouter.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);

            VirtualNetwork<Link> virtualNetwork = inject.getModal(new TypeLiteral<VirtualNetwork<Link>>() {
            });

            TravelData travelData = inject.getModal(TravelData.class);

            AbstractVirtualNodeDest abstractVirtualNodeDest = new RandomVirtualNodeDest();
            AbstractRoboTaxiDestMatcher abstractVehicleDestMatcher = new GlobalBipartiteMatching(EuclideanDistanceCost.INSTANCE);
            return new FeedforwardFluidicTimeVaryingRebalancingPolicy(config, operatorConfig, travelTime, router, eventsManager, network, virtualNetwork, abstractVirtualNodeDest,
                    abstractVehicleDestMatcher, travelData, db);
        }
    }
}