/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.tshare;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
import ch.ethz.idsc.amodeus.dispatcher.core.SharedPartitionedDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.shared.OnboardRequests;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMenu;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractRoboTaxiDestMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractVirtualNodeDest;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceHeuristics;
import ch.ethz.idsc.amodeus.dispatcher.util.GlobalBipartiteMatching;
import ch.ethz.idsc.amodeus.dispatcher.util.RandomVirtualNodeDest;
import ch.ethz.idsc.amodeus.dispatcher.util.SharedBipartiteMatchingUtils;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.routing.CachedNetworkTimeDistance;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.amodeus.routing.EasyMinDistPathCalculator;
import ch.ethz.idsc.amodeus.routing.EasyMinTimePathCalculator;
import ch.ethz.idsc.amodeus.routing.EuclideanDistanceFunction;
import ch.ethz.idsc.amodeus.routing.TimeDistanceProperty;
import ch.ethz.idsc.amodeus.util.geo.FastQuadTree;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;

/** Ma, Shuo, Yu Zheng, and Ouri Wolfson. "T-share: A large-scale dynamic taxi ridesharing service."
 * Data Engineering (ICDE), 2013 IEEE 29th International Conference on. IEEE, 2013.
 * 
 * Changes compared to the original version:
 * - The version presented in the publication considers only the addition of 1 trip to a trip which is
 * already being transported by a taxi. In order to operate the policy with taxis with capacity N, in this
 * version the time windows of all requests already in a taxi are checked before the insertion of a
 * new request is allowed.
 * - To limit computation time, a maximum legth of the planned {@link SharedMenu} was introduced. */
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
    private final Scalar pickupDelayMax;
    private final Scalar drpoffDelayMax;
    private final double menuHorizon;
    private final DualSideSearch dualSideSearch;
    private final CachedNetworkTimeDistance distanceCashed;
    private final CachedNetworkTimeDistance travelTimeCalculator;

    protected TShareDispatcher(Network network, //
            Config config, AVDispatcherConfig avDispatcherConfig, //
            TravelTime travelTime, AVRouter router, EventsManager eventsManager, //
            MatsimAmodeusDatabase db, //
            VirtualNetwork<Link> virtualNetwork) {
        super(config, avDispatcherConfig, travelTime, router, eventsManager, virtualNetwork, db);
        SafeConfig safeConfig = SafeConfig.wrap(avDispatcherConfig);
        dispatchPeriod = safeConfig.getInteger("dispatchPeriod", 30);
        rebalancePeriod = safeConfig.getInteger("rebalancingPeriod", 1800);
        this.network = network;
        DispatcherConfig dispatcherConfig = DispatcherConfig.wrap(avDispatcherConfig);
        DistanceHeuristics distanceHeuristics = //
                dispatcherConfig.getDistanceHeuristics(DistanceHeuristics.EUCLIDEAN);
        System.out.println("Using DistanceHeuristics: " + distanceHeuristics.name());
        distanceFunction = distanceHeuristics.getDistanceFunction(network);
        distanceCashed = //
                new CachedNetworkTimeDistance(EasyMinDistPathCalculator.prepPathCalculator(network, new FastAStarLandmarksFactory()), 180000.0, TimeDistanceProperty.INSTANCE);
        travelTimeCalculator = //
                new CachedNetworkTimeDistance(EasyMinTimePathCalculator.prepPathCalculator(network, new FastAStarLandmarksFactory()), 180000.0, TimeDistanceProperty.INSTANCE);
        bipartiteMatchingUtils = new SharedBipartiteMatchingUtils(network);

        /** T-Share specific */
        pickupDelayMax = Quantity.of(safeConfig.getInteger("pickupDelayMax", 10 * 60), SI.SECOND);
        drpoffDelayMax = Quantity.of(safeConfig.getInteger("drpoffDelayMax", 30 * 60), SI.SECOND);
        menuHorizon = safeConfig.getDouble("menuHorizon", 1.5);

        /** initialize grid with T-cells */
        QuadTree<Link> linkTree = FastQuadTree.of(network);
        for (VirtualNode<Link> virtualNode : virtualNetwork.getVirtualNodes()) {
            System.out.println("preparing grid cell: " + virtualNode.getIndex());
            gridCells.put(virtualNode, new GridCell(virtualNode, virtualNetwork, network, distanceCashed, travelTimeCalculator, linkTree, 0.0));
        }
        dualSideSearch = new DualSideSearch(gridCells, virtualNetwork, network);
        System.out.println("According to the reference, a rectangular {@link VirtualNetwork} should be used.");
        System.out.println("Ensure that VirtualNetworkCreators.RECTANGULAR is used.");
    }

    @Override
    protected void redispatch(double now) {
        final long round_now = Math.round(now);
        if (round_now % dispatchPeriod == 0) {

            /** unit capacity dispatching for all divertable vehicles with zero passengers on board,
             * in this implementation, global bipartite matching is used */
            Collection<RoboTaxi> divertableAndEmpty = getDivertableRoboTaxis().stream().filter(rt -> (rt.getUnmodifiableViewOfCourses().size() == 0))//
                    .collect(Collectors.toList());
            printVals = bipartiteMatchingUtils.executePickup(this, this::getCurrentPickupTaxi, divertableAndEmpty, //
                    getAVRequests(), distanceFunction, network);

            /** update the roboTaxi planned locations */
            Collection<RoboTaxi> customerCarrying = getDivertableRoboTaxis().stream()//
                    .filter(rt -> OnboardRequests.getMenuOnBoardCustomers(rt) >= 1)//
                    .filter(rt -> (rt.getCapacity() - OnboardRequests.getMenuOnBoardCustomers(rt)) >= 1)//
                    .filter(OnboardRequests::canPickupNewCustomer)//
                    .collect(Collectors.toList());

            Map<VirtualNode<Link>, Set<RoboTaxi>> plannedLocations = //
                    RoboTaxiPlannedLocations.of(customerCarrying, virtualNetwork);

            /** do T-share ridesharing */
            List<AVRequest> sortedRequests = getAVRequests().stream()//
                    .filter(avr -> !getCurrentPickupAssignements().keySet().contains(avr))//
                    .sorted(RequestWaitTimeComparator.INSTANCE)//
                    .collect(Collectors.toList());

            for (AVRequest avr : sortedRequests) {
                Scalar latestPickup = LatestPickup.of(avr, pickupDelayMax);
                Scalar latestArrval = LatestArrival.of(avr, drpoffDelayMax, travelTimeCalculator, now);

                /** dual side search */
                Collection<RoboTaxi> potentialTaxis = //
                        dualSideSearch.apply(avr, plannedLocations, latestPickup, latestArrval);

                /** insertion feasibility check */
                NavigableMap<Scalar, InsertionCheck> insertions = new TreeMap<>();
                for (RoboTaxi taxi : potentialTaxis) {
                    if (taxi.getUnmodifiableViewOfCourses().size() < taxi.getCapacity() * 2 * menuHorizon) {
                        InsertionCheck check = new InsertionCheck(distanceCashed, travelTimeCalculator, taxi, avr, //
                                pickupDelayMax, drpoffDelayMax, now);
                        if (Objects.nonNull(check.getAddDistance()))
                            insertions.put(check.getAddDistance(), check);
                    }
                }

                /** plan update */
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
            /** potentially rebalancing? Possibly rebalancing makes sense, however not stated in paper. */
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
