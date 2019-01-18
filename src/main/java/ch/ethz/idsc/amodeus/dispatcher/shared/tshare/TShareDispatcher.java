/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.collections.QuadTree;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.DispatcherConfig;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiUtils;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedPartitionedDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractRoboTaxiDestMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractVirtualNodeDest;
import ch.ethz.idsc.amodeus.dispatcher.util.BipartiteMatchingUtils;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceHeuristics;
import ch.ethz.idsc.amodeus.dispatcher.util.EuclideanDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.GlobalBipartiteMatching;
import ch.ethz.idsc.amodeus.dispatcher.util.NetworkDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.NetworkMinDistDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.NetworkMinTimeDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.RandomVirtualNodeDest;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;

public class TShareDispatcher extends SharedPartitionedDispatcher {

    /** general */
    private final int dispatchPeriod;
    private final int rebalancePeriod;
    private final Network network;
    private final DistanceFunction distanceFunction;
    private final BipartiteMatchingUtils bipartiteMatchingUtils;

    /** T-Share specific */
    private final Map<VirtualNode<Link>, GridCell> gridCells = new HashMap<>();
    private final double pickupDelayMax;
    private final double drpoffDelayMax;
    private final DualSideSearch dualSideSearch;

    protected TShareDispatcher(Network network, //
            Config config, AVDispatcherConfig avDispatcherConfig, //
            TravelTime travelTime, AVRouter router, EventsManager eventsManager, //
            MatsimAmodeusDatabase db, //
            VirtualNetwork<Link> virtualNetwork) {
        super(config, avDispatcherConfig, travelTime, router, eventsManager, virtualNetwork, db);
        /** general parameters */
        SafeConfig safeConfig = SafeConfig.wrap(avDispatcherConfig);
        dispatchPeriod = safeConfig.getInteger("dispatchPeriod", 30);
        rebalancePeriod = safeConfig.getInteger("rebalancingPeriod", 1800);
        this.network = network;
        DispatcherConfig dispatcherConfig = DispatcherConfig.wrap(avDispatcherConfig);
        DistanceHeuristics distanceHeuristics = //
                dispatcherConfig.getDistanceHeuristics(DistanceHeuristics.EUCLIDEAN);
        System.out.println("Using DistanceHeuristics: " + distanceHeuristics.name());
        distanceFunction = distanceHeuristics.getDistanceFunction(network);

        bipartiteMatchingUtils = new BipartiteMatchingUtils(network);

        /** T-Share specific */
        pickupDelayMax = safeConfig.getInteger("pickupDelayMax", 10 * 60);
        drpoffDelayMax = safeConfig.getInteger("drpoffDelayMax", 30 * 60);

        /** initialize grid with T-cells */
        NetworkDistanceFunction minDist = new NetworkMinDistDistanceFunction(network, new FastAStarLandmarksFactory());
        NetworkDistanceFunction minTime = new NetworkMinTimeDistanceFunction(network, new FastAStarLandmarksFactory());
        QuadTree<Link> linkTree = FastQuadTree.of(network);
        for (VirtualNode<Link> virtualNode : virtualNetwork.getVirtualNodes()) {
            gridCells.put(virtualNode, new GridCell(virtualNode, virtualNetwork, network, minDist, minTime, linkTree));
        }

        dualSideSearch = new DualSideSearch(gridCells, virtualNetwork, pickupDelayMax, drpoffDelayMax, network);

        System.exit(1);

        // TODO ensure that rectangular grid was used
    }

    @Override
    protected void redispatch(double now) {
        final long round_now = Math.round(now);
        if (round_now % dispatchPeriod == 0) {

            /** unit capacity dispatching for all divertable vehicles with zero passengers on board,
             * for now global bipartite matching is used */
            printVals = bipartiteMatchingUtils.executePickup(this, getDivertableRoboTaxis(), //
                    getAVRequests(), distanceFunction, network);

            /** update the roboTaxi planned locations */
            Collection<RoboTaxi> customerCarrying = getDivertableRoboTaxis().stream()//
                    .filter(rt -> RoboTaxiUtils.getNumberOnBoardRequests(rt) > 1)//
                    .collect(Collectors.toList());
            Map<VirtualNode<Link>, Set<RoboTaxi>> plannedLocations = //
                    RoboTaxiPlannedLocations.of(customerCarrying, virtualNetwork);

            /** do T-share ridesharing */
            // TODO sorted according to submission time.
            for (AVRequest avr : getAVRequests()) {
                Collection<RoboTaxi> potentialTaxis = dualSideSearch.apply(avr, plannedLocations);
                NavigableMap<Double, InsertionCheck> insertions = new TreeMap<>();
                for (RoboTaxi roboTaxi : potentialTaxis) {
                    InsertionCheck check = new InsertionCheck(roboTaxi, avr, pickupDelayMax, drpoffDelayMax);
                    if (Objects.nonNull(check.getAddDistance()))
                        insertions.put(check.getAddDistance(), check);
                }

                if (Objects.nonNull(insertions.firstEntry())) {
                    /** insert the request into the plan of the {@link RoboTaxi} */
                    insertions.firstEntry().getValue().insert();
                    /** remove the {@link RoboTaxi} so that it does not get assigned again in the same round */
                    RoboTaxi sentTaxi = insertions.firstEntry().getValue().getRoboTaxi();
                    plannedLocations.values().stream().forEach(rtc -> {
                        rtc.remove(sentTaxi);
                    });
                }
            }
        }

        /** dispatching of available {@link RoboTaxi}s to the equator */
        if (round_now % rebalancePeriod == 0) {
            /** potentially rebalancing? TODO maybe rebalancing makes sense, however not stated in paper. */
        }
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

        @Inject
        private Config config;

        @Inject
        private MatsimAmodeusDatabase db;

        @Inject
        private VirtualNetwork<Link> virtualNetwork;

        @Override
        public AVDispatcher createDispatcher(AVDispatcherConfig avconfig, AVRouter router) {
            @SuppressWarnings("unused")
            AVGeneratorConfig generatorConfig = avconfig.getParent().getGeneratorConfig();

            @SuppressWarnings("unused")
            AbstractVirtualNodeDest abstractVirtualNodeDest = new RandomVirtualNodeDest();
            @SuppressWarnings("unused")
            AbstractRoboTaxiDestMatcher abstractVehicleDestMatcher = new GlobalBipartiteMatching(EuclideanDistanceFunction.INSTANCE);

            return new TShareDispatcher(network, config, avconfig, travelTime, router, eventsManager, db, virtualNetwork);
        }
    }
}
