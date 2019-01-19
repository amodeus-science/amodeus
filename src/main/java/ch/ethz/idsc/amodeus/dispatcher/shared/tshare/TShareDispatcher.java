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
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiUtils;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedPartitionedDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractRoboTaxiDestMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractVirtualNodeDest;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceHeuristics;
import ch.ethz.idsc.amodeus.dispatcher.util.EuclideanDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.GlobalBipartiteMatching;
import ch.ethz.idsc.amodeus.dispatcher.util.NetworkDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.NetworkMinDistDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.NetworkMinTimeDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.RandomVirtualNodeDest;
import ch.ethz.idsc.amodeus.dispatcher.util.SharedBipartiteMatchingUtils;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;

/** Ma, Shuo, Yu Zheng, and Ouri Wolfson. "T-share: A large-scale dynamic taxi ridesharing service."
 * Data Engineering (ICDE), 2013 IEEE 29th International Conference on. IEEE, 2013. */
public class TShareDispatcher extends SharedPartitionedDispatcher {

    /** general */
    private final int dispatchPeriod;
    private final int rebalancePeriod;
    private final Network network;
    private final DistanceFunction distanceFunction;
    private final SharedBipartiteMatchingUtils bipartiteMatchingUtils;
    private Tensor printVals = Tensors.empty();

    /** T-Share specific */
    private final Map<VirtualNode<Link>, GridCell> gridCells = new HashMap<>();
    private final double pickupDelayMax;
    private final double drpoffDelayMax;
    private final DualSideSearch dualSideSearch;
    private final NetworkDistanceFunction distance;

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

        bipartiteMatchingUtils = new SharedBipartiteMatchingUtils(network);

        /** T-Share specific */
        pickupDelayMax = safeConfig.getInteger("pickupDelayMax", 10 * 60);
        drpoffDelayMax = safeConfig.getInteger("drpoffDelayMax", 30 * 60);
        this.distance = new NetworkMinTimeDistanceFunction(network, new FastAStarLandmarksFactory());

        /** initialize grid with T-cells */
        NetworkDistanceFunction minDist = new NetworkMinDistDistanceFunction(network, new FastAStarLandmarksFactory());
        NetworkDistanceFunction minTime = new NetworkMinTimeDistanceFunction(network, new FastAStarLandmarksFactory());
        QuadTree<Link> linkTree = FastQuadTree.of(network);
        for (VirtualNode<Link> virtualNode : virtualNetwork.getVirtualNodes()) {
            gridCells.put(virtualNode, new GridCell(virtualNode, virtualNetwork, network, minDist, minTime, linkTree));
        }

        dualSideSearch = new DualSideSearch(gridCells, virtualNetwork, pickupDelayMax, drpoffDelayMax, network);

        // TODO ensure that rectangular grid was used to be in accordance with reference.
    }

    @Override
    protected void redispatch(double now) {
        final long round_now = Math.round(now);
        if (round_now % dispatchPeriod == 0) {

            // TODO previously I have put bipartite matching here, now trying with a more simple strategy
            // /** unit capacity dispatching for all divertable vehicles with zero passengers on board,
            // * for now global bipartite matching is used */
            // printVals = bipartiteMatchingUtils.executePickup(this, this::getCurrentPickupTaxi, getDivertableRoboTaxis(), //
            // getAVRequests(), distanceFunction, network);

            if (getRoboTaxiSubset(RoboTaxiStatus.STAY).size() > 0) {
                RoboTaxi roboTaxi = getRoboTaxiSubset(RoboTaxiStatus.STAY).iterator().next();
                if (getAVRequests().size() > 0) {
                    AVRequest avr = getAVRequests().iterator().next();
                    if (!getCurrentPickupAssignements().keySet().contains(avr))
                        addSharedRoboTaxiPickup(roboTaxi, avr);
                }

            }

            /** update the roboTaxi planned locations */
            Collection<RoboTaxi> customerCarrying = getDivertableRoboTaxis().stream()//
                    .filter(rt -> RoboTaxiUtils.getNumberOnBoardRequests(rt) >= 1)//
                    .filter(RoboTaxiUtils::canPickupNewCustomer)//
                    .collect(Collectors.toList());

            Map<VirtualNode<Link>, Set<RoboTaxi>> plannedLocations = //
                    RoboTaxiPlannedLocations.of(customerCarrying, virtualNetwork);

            /** do T-share ridesharing */
            // TODO sorted according to submission time.
            for (AVRequest avr : getAVRequests()) {
                if (getCurrentPickupAssignements().keySet().contains(avr))
                    continue;

                double latestPickup = avr.getSubmissionTime() + pickupDelayMax;
                double latestArrval = distance.getTravelTime(avr.getFromLink(), avr.getToLink())//
                        + drpoffDelayMax;

                Collection<RoboTaxi> potentialTaxis = //
                        dualSideSearch.apply(avr, plannedLocations, latestPickup, latestArrval);
                NavigableMap<Double, InsertionCheck> insertions = new TreeMap<>();
                for (RoboTaxi roboTaxi : potentialTaxis) {
                    System.out.println("made insertion check");
                    InsertionCheck check = new InsertionCheck(distance, //
                            roboTaxi, avr, pickupDelayMax, drpoffDelayMax);
                    if (Objects.nonNull(check.getAddDistance()))
                        insertions.put(check.getAddDistance(), check);
                }

                if (Objects.nonNull(insertions.firstEntry())) {
                    /** insert the request into the plan of the {@link RoboTaxi} */
                    insertions.firstEntry().getValue().insert(this::addSharedRoboTaxiPickup);
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

    @Override
    protected String getInfoLine() {
        return String.format("%s H=%s", //
                super.getInfoLine(), //
                printVals.toString() /** This is where Dispatcher@ V... R... MR.. H is printed on console */
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
