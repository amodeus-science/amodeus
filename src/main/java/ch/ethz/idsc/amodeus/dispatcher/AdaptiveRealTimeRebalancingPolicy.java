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
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractRoboTaxiDestMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractVirtualNodeDest;
import ch.ethz.idsc.amodeus.dispatcher.util.BipartiteMatchingUtils;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceHeuristics;
import ch.ethz.idsc.amodeus.dispatcher.util.EuclideanDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.FeasibleRebalanceCreator;
import ch.ethz.idsc.amodeus.dispatcher.util.GlobalBipartiteMatching;
import ch.ethz.idsc.amodeus.dispatcher.util.RandomVirtualNodeDest;
import ch.ethz.idsc.amodeus.lp.LPMinFlow;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualLink;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.VirtualNode;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.red.Total;
import ch.ethz.idsc.tensor.sca.Round;
import ch.ethz.idsc.tensor.sca.Sign;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;

/** Implementation of the "Adaptive Real-Time Rebalancing Policy" presented in
 * Pavone, M., Smith, S.L., Frazzoli, E. and Rus, D., 2012.
 * Robotic load balancing for mobility-on-demand systems.
 * The International Journal of Robotics Research, 31(7), pp.839-854. */
public class AdaptiveRealTimeRebalancingPolicy extends PartitionedDispatcher {
    private final AbstractVirtualNodeDest virtualNodeDest;
    private final AbstractRoboTaxiDestMatcher vehicleDestMatcher;
    private final LPMinFlow lpMinFlow;
    private final DistanceFunction distanceFunction;
    private final DistanceHeuristics distanceHeuristics;
    private final Network network;
    private final BipartiteMatchingUtils bipartiteMatchingEngine;
    private final int rebalancingPeriod;
    private final int dispatchPeriod;
    private final int numRobotaxi;
    // ---
    private Tensor printVals = Tensors.empty();
    private int total_rebalanceCount = 0;
    private boolean started = false;

    public AdaptiveRealTimeRebalancingPolicy( //
            Config config, AVDispatcherConfig avDispatcherConfig, //
            AVGeneratorConfig generatorConfig, TravelTime travelTime, //
            AVRouter router, EventsManager eventsManager, //
            Network network, VirtualNetwork<Link> virtualNetwork, //
            AbstractVirtualNodeDest abstractVirtualNodeDest, //
            AbstractRoboTaxiDestMatcher abstractVehicleDestMatcher, //
            MatsimAmodeusDatabase db) {
        super(config, avDispatcherConfig, travelTime, router, eventsManager, virtualNetwork, db);
        virtualNodeDest = abstractVirtualNodeDest;
        vehicleDestMatcher = abstractVehicleDestMatcher;
        numRobotaxi = (int) generatorConfig.getNumberOfVehicles();
        lpMinFlow = new LPMinFlow(virtualNetwork);
        lpMinFlow.initiateLP();
        DispatcherConfig dispatcherConfig = DispatcherConfig.wrap(avDispatcherConfig);
        dispatchPeriod = dispatcherConfig.getDispatchPeriod(30);
        rebalancingPeriod = dispatcherConfig.getRebalancingPeriod(300);
        this.network = network;
        distanceHeuristics = dispatcherConfig.getDistanceHeuristics(DistanceHeuristics.EUCLIDEAN);
        this.bipartiteMatchingEngine = new BipartiteMatchingUtils(network);
        System.out.println("Using DistanceHeuristics: " + distanceHeuristics.name());
        this.distanceFunction = distanceHeuristics.getDistanceFunction(network);
    }

    @Override
    public void redispatch(double now) {
        /** PART I: rebalance all vehicles periodically */
        long round_now = Math.round(now);

        if (!started) {
            if (getRoboTaxis().size() == 0) /** return if the roboTaxis are not ready yet */
                return;
            /** as soon as the roboTaxis are ready, make sure to execute rebalancing and dispatching for now=0 */
            round_now = 0;
            started = true;
        }

        /** necessary because robotaxis not presence at time zero */
        if (round_now % rebalancingPeriod == 0) {

            Map<VirtualNode<Link>, List<AVRequest>> requests = getVirtualNodeRequests();
            /** compute rebalancing vehicles and send to virtualNodes */
            {
                Map<VirtualNode<Link>, List<RoboTaxi>> availableVehicles = getVirtualNodeDivertableNotRebalancingRoboTaxis();
                int totalAvailable = 0;
                for (List<RoboTaxi> robotaxiList : availableVehicles.values()) {
                    totalAvailable += robotaxiList.size();
                }

                /** calculate desired vehicles per vNode */
                int num_requests = requests.values().stream().mapToInt(List::size).sum();
                double vi_desired_num = ((numRobotaxi - num_requests) / (double) virtualNetwork.getvNodesCount());
                int vi_desired_numint = (int) Math.floor(vi_desired_num);
                Tensor vi_desiredT = Tensors.vector(i -> RationalScalar.of(vi_desired_numint, 1), virtualNetwork.getvNodesCount());

                /** calculate excess vehicles per virtual Node i, where
                 * v_i excess = vi_own - c_i = v_i + sum_j (v_ji) - c_i */
                Map<VirtualNode<Link>, List<RoboTaxi>> v_ij_reb = getVirtualNodeRebalancingToRoboTaxis();
                Map<VirtualNode<Link>, List<RoboTaxi>> v_ij_cust = getVirtualNodeArrivingWithCustomerRoboTaxis();

                /** this was necessary for some scenarios resulting in undivertable vehicles to ensure
                 * LP feasibility, will not be triggered in most cases */
                double diffDueUnDivertable = (getRoboTaxiSubset(RoboTaxiStatus.STAY).size() + //
                        getRoboTaxiSubset(RoboTaxiStatus.DRIVETOCUSTOMER).size() - //
                        getDivertableNotRebalancingRoboTaxis().size()) / ((double) virtualNetwork.getvNodesCount());
                int diffCeil = (int) Math.ceil(diffDueUnDivertable);

                Tensor vi_excessT = Array.zeros(virtualNetwork.getvNodesCount());
                for (VirtualNode<Link> virtualNode : availableVehicles.keySet()) {
                    int viExcessVal = availableVehicles.get(virtualNode).size() + v_ij_reb.get(virtualNode).size() + v_ij_cust.get(virtualNode).size() + diffCeil
                            - requests.get(virtualNode).size();
                    vi_excessT.set(RealScalar.of(viExcessVal), virtualNode.getIndex());
                }

                /** solve the linear program with updated right-hand side */
                Tensor rhs = vi_desiredT.subtract(vi_excessT);
                Tensor rebalanceCount2 = Tensors.empty();
                if (totalAvailable > 0) {
                    lpMinFlow.solveLP(false, rhs);
                    rebalanceCount2 = lpMinFlow.getAlphaAbsolute_ij();
                } else {
                    rebalanceCount2 = Array.zeros(virtualNetwork.getvNodesCount(), virtualNetwork.getvNodesCount());
                }
                Tensor rebalanceCount = Round.of(rebalanceCount2);

                /** assert that solution is integer and does not contain negative values */
                GlobalAssert.that(rebalanceCount.flatten(-1).map(Scalar.class::cast).allMatch(Sign::isPositiveOrZero));

                /** ensure that not more vehicles are sent away than available */
                Tensor feasibleRebalanceCount = FeasibleRebalanceCreator.returnFeasibleRebalance(rebalanceCount.unmodifiable(), availableVehicles);
                total_rebalanceCount += (Integer) ((Scalar) Total.of(Tensor.of(feasibleRebalanceCount.flatten(-1)))).number();

                /** generate routing instructions for rebalancing vehicles */
                Map<VirtualNode<Link>, List<Link>> rebalanceDestinations = virtualNetwork.createVNodeTypeMap();

                /** fill rebalancing destinations */
                for (int i = 0; i < virtualNetwork.getvLinksCount(); ++i) {
                    VirtualLink<Link> virtualLink = this.virtualNetwork.getVirtualLink(i);
                    VirtualNode<Link> toNode = virtualLink.getTo();
                    VirtualNode<Link> fromNode = virtualLink.getFrom();
                    int numreb = (Integer) (feasibleRebalanceCount.Get(fromNode.getIndex(), toNode.getIndex())).number();
                    List<Link> rebalanceTargets = virtualNodeDest.selectLinkSet(toNode, numreb);
                    rebalanceDestinations.get(fromNode).addAll(rebalanceTargets);
                }

                /** consistency check: rebalancing destination links must not exceed available vehicles in virtual node */
                Map<VirtualNode<Link>, List<RoboTaxi>> finalAvailableVehicles = availableVehicles;
                GlobalAssert.that(virtualNetwork.getVirtualNodes().stream().allMatch(v -> finalAvailableVehicles.get(v).size() >= rebalanceDestinations.get(v).size()));

                /** send rebalancing vehicles using the setVehicleRebalance command */
                for (VirtualNode<Link> virtualNode : rebalanceDestinations.keySet()) {
                    Map<RoboTaxi, Link> rebalanceMatching = vehicleDestMatcher.matchLink(availableVehicles.get(virtualNode), rebalanceDestinations.get(virtualNode));
                    rebalanceMatching.keySet().forEach(v -> setRoboTaxiRebalance(v, rebalanceMatching.get(v)));
                }
            }
        }

        /** Part II: outside rebalancing periods, permanently assign destinations to vehicles using bipartite matching */
        if (round_now % dispatchPeriod == 0) {
            printVals = bipartiteMatchingEngine.executePickup(this, getDivertableRoboTaxis(), //
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

        @Inject
        private Config config;

        @Inject
        private MatsimAmodeusDatabase db;

        @Override
        public AVDispatcher createDispatcher(AVDispatcherConfig avconfig, AVRouter router) {
            AVGeneratorConfig generatorConfig = avconfig.getParent().getGeneratorConfig();

            AbstractVirtualNodeDest abstractVirtualNodeDest = new RandomVirtualNodeDest();
            AbstractRoboTaxiDestMatcher abstractVehicleDestMatcher = new GlobalBipartiteMatching(EuclideanDistanceFunction.INSTANCE);

            return new AdaptiveRealTimeRebalancingPolicy( //
                    config, avconfig, generatorConfig, travelTime, //
                    router, eventsManager, network, virtualNetwork, //
                    abstractVirtualNodeDest, abstractVehicleDestMatcher, db);
        }
    }
}
