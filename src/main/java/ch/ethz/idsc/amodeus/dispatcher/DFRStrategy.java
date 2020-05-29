/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.matsim.amodeus.components.AVDispatcher;
import org.matsim.amodeus.components.AVRouter;
import org.matsim.amodeus.config.AmodeusModeConfig;
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
import ch.ethz.idsc.amodeus.dispatcher.util.ConfigurableBipartiteMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceCost;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceHeuristics;
import ch.ethz.idsc.amodeus.dispatcher.util.EuclideanDistanceCost;
import ch.ethz.idsc.amodeus.dispatcher.util.FeasibleRebalanceCreator;
import ch.ethz.idsc.amodeus.dispatcher.util.GlobalBipartiteMatching;
import ch.ethz.idsc.amodeus.dispatcher.util.MetropolisHastings;
import ch.ethz.idsc.amodeus.dispatcher.util.OwnedRoboTaxis;
import ch.ethz.idsc.amodeus.dispatcher.util.RandomVirtualNodeDest;
import ch.ethz.idsc.amodeus.dispatcher.util.Rounder;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.amodeus.traveldata.TravelData;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.virtualnetwork.Neighboring;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.io.Export;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.red.Mean;

/** Implementation of the "DFR algorithm" presented in
 * Albert, M., Ruch, C. and Frazzoli, E., 2019.
 * Imbalance in Mobility-on-Demand Systems: A Stochastic Model and Distributed Control Approach.
 * ACM Transactions on Spatial Algorithms and Systems (TSAS) - Special Issue on Urban Mobility: Algorithms and Systems, 5(2), article no. 13. */
public class DFRStrategy extends PartitionedDispatcher {
    private final int dispatchPeriod;
    private final int rebalancingPeriod;
    private Tensor printVals = Tensors.empty();
    private final DistanceFunction distanceFunction;
    private final Network network;
    /** travelData is the feedforward signal used by the DFR dispatcher */
    private final TravelData travelData;
    private final ConfigurableBipartiteMatcher bipartiteMatchingUtils;
    private final Neighboring neighboring;
    private final OwnedRoboTaxis ownedRoboTaxis;
    private final Rounder rounder;
    private final Tensor metropolisHastings;
    private final AbstractRoboTaxiDestMatcher vehicleDestMatcher;
    private final AbstractVirtualNodeDest virtualNodeDest;
    private final Scalar epsilon = RealScalar.of(0.05);
    private final boolean doDFR;
    private final Tensor totalTimes = Tensors.empty();
    private final Tensor feedbackTimes = Tensors.empty();
    private final Config config;
    private boolean haveExported = false;

    private DFRStrategy(Network network, VirtualNetwork<Link> virtualNetwork, Config config, //
            AmodeusModeConfig operatorConfig, TravelTime travelTime, //
            AVRouter router, EventsManager eventsManager, TravelData travelData, //
            MatsimAmodeusDatabase db) {
        super(config, operatorConfig, travelTime, router, eventsManager, virtualNetwork, db);
        DispatcherConfigWrapper dispatcherConfig = DispatcherConfigWrapper.wrap(operatorConfig.getDispatcherConfig());
        dispatchPeriod = dispatcherConfig.getDispatchPeriod(30);
        rebalancingPeriod = dispatcherConfig.getRebalancingPeriod(300);
        DistanceHeuristics distanceHeuristics = //
                dispatcherConfig.getDistanceHeuristics(DistanceHeuristics.EUCLIDEAN);
        System.out.println("Using DistanceHeuristics: " + distanceHeuristics.name());
        distanceFunction = distanceHeuristics.getDistanceFunction(network);
        this.network = network;
        this.travelData = travelData;
        this.neighboring = new Neighboring(virtualNetwork, network);
        this.ownedRoboTaxis = new OwnedRoboTaxis(virtualNetwork);
        this.rounder = new Rounder(virtualNetwork);
        this.metropolisHastings = new MetropolisHastings(virtualNetwork, neighboring).getAll();
        this.vehicleDestMatcher = new GlobalBipartiteMatching(EuclideanDistanceCost.INSTANCE);
        this.virtualNodeDest = new RandomVirtualNodeDest();
        SafeConfig safeConfig = SafeConfig.wrap(operatorConfig);
        bipartiteMatchingUtils = new ConfigurableBipartiteMatcher(network, new DistanceCost(distanceFunction), safeConfig);
        this.doDFR = dispatcherConfig.getBoolStrict("DFR");
        this.config = config;
        System.out.println("DFR is set to: " + doDFR);
        System.out.println("travelData: " + travelData.getLPName());
    }

    @Override
    public void redispatch(double now) {
        long time = System.currentTimeMillis();

        final long round_now = Math.round(now);

        /** assigning vehicles to requests, simply a bipartite matching */
        if (round_now % dispatchPeriod == 0)
            printVals = bipartiteMatchingUtils.executePickup(this, getDivertableRoboTaxis(), //
                    getPassengerRequests(), distanceFunction, network);

        /** rebalancing contributions */
        long contribDFR = 0;
        if (round_now % rebalancingPeriod == 0 && travelData.coversTime(round_now)) {
            /** update data structures */
            ownedRoboTaxis.update(getRoboTaxis());
            Map<VirtualNode<Link>, Scalar> imbalance = getImbalances();

            /** compute new rebalancing contributions for all sets of virtual nodes */
            for (VirtualNode<Link> from : virtualNetwork.getVirtualNodes())
                for (VirtualNode<Link> to : virtualNetwork.getVirtualNodes())
                    if (!from.equals(to)) {
                        /** feedforward part */
                        Scalar reb = travelData.getAlphaRateAtTime((int) round_now).Get(from.getIndex(), to.getIndex()) //
                                .multiply(RealScalar.of(rebalancingPeriod));
                        boolean isNeighboring = neighboring.check(from, to);

                        /** dfr feedback part */
                        long timeDFR = System.currentTimeMillis();
                        if (isNeighboring && doDFR) {
                            Scalar wij = metropolisHastings.Get(from.getIndex(), to.getIndex());
                            Scalar contribution = epsilon.multiply(wij).multiply(imbalance.get(to).subtract(imbalance.get(from)));
                            if (Scalars.lessEquals(RealScalar.ZERO, contribution))
                                reb = reb.add(contribution);
                        }
                        contribDFR = contribDFR + (System.currentTimeMillis() - timeDFR);

                        /** rounding and making a rebalance command */
                        rounder.addContribution(from, to, reb);
                    }
            feedbackTimes.append(Quantity.of(contribDFR * 0.001, "s"));

            /** complete rebalancing to execute (integral part) */
            Tensor rebalancingToExecute = Array.zeros(virtualNetwork.getvNodesCount(), virtualNetwork.getvNodesCount());
            for (VirtualNode<Link> from : virtualNetwork.getVirtualNodes())
                for (VirtualNode<Link> to : virtualNetwork.getVirtualNodes()) {
                    Scalar reb = rounder.removeIntegral(from, to);
                    int fromInd = from.getIndex();
                    int toInd = to.getIndex();
                    rebalancingToExecute.set(reb, fromInd, toInd);
                }

            /** ensure feasible rebalance solution and execute */
            Map<VirtualNode<Link>, List<RoboTaxi>> availableVehicles = getVirtualNodeDivertableNotRebalancingRoboTaxis();
            Tensor feasibleRebalanceCount = FeasibleRebalanceCreator.returnFeasibleRebalance(rebalancingToExecute.unmodifiable(), availableVehicles);

            /** generate routing instructions for rebalancing vehicles */
            Map<VirtualNode<Link>, List<Link>> destinationLinks = virtualNetwork.createVNodeTypeMap();

            /** fill rebalancing destinations */
            for (VirtualNode<Link> fromNode : virtualNetwork.getVirtualNodes())
                for (VirtualNode<Link> toNode : virtualNetwork.getVirtualNodes()) {
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
        }

        totalTimes.append(Quantity.of((System.currentTimeMillis() - time) * 0.001, "s"));

        if (!haveExported && round_now >= 107000) { // TODO @sebhoerl check hardcoded
            try {
                Export.of(new File(config.controler().getOutputDirectory() + "/dfrTimes.csv"), feedbackTimes);
                Export.of(new File(config.controler().getOutputDirectory() + "/totalTimes.csv"), totalTimes);
                System.out.println("feedbackTimes,m: " + Mean.of(feedbackTimes));
                System.out.println("totalTimes,m: " + Mean.of(totalTimes));
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
            haveExported = true;
        }
    }

    private Map<VirtualNode<Link>, Scalar> getImbalances() {
        return virtualNetwork.getVirtualNodes().stream()
                .collect(Collectors.toMap(vNode -> vNode, vNode -> RealScalar.of(getVirtualNodeRequests().get(vNode).size() - ownedRoboTaxis.in(vNode).size())));
    }

    @Override
    protected String getInfoLine() {
        return String.format("%s H=%s", //
                super.getInfoLine(), //
                printVals.toString() // This is where Dispatcher@ V... R... MR.. H is printed on console
        );
    }

    public static class Factory implements AVDispatcherFactory {
        @Override
        public AVDispatcher createDispatcher(InstanceGetter inject) {
            Config config = inject.get(Config.class);
            MatsimAmodeusDatabase db = inject.get(MatsimAmodeusDatabase.class);
            EventsManager eventsManager = inject.get(EventsManager.class);

            AmodeusModeConfig operatorConfig = inject.getModal(AmodeusModeConfig.class);
            Network network = inject.getModal(Network.class);
            AVRouter router = inject.getModal(AVRouter.class);
            TravelTime travelTime = inject.getModal(TravelTime.class);

            VirtualNetwork<Link> virtualNetwork = inject.getModal(new TypeLiteral<VirtualNetwork<Link>>() {
            });

            TravelData travelData = inject.getModal(TravelData.class);

            return new DFRStrategy(network, virtualNetwork, config, operatorConfig, travelTime, router, //
                    eventsManager, travelData, db);
        }
    }
}
