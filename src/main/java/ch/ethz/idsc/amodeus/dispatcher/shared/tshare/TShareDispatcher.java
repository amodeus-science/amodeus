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

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.collections.QuadTree;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.DispatcherConfigWrapper;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedPartitionedDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.shared.OnMenuRequests;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedMenu;
import ch.ethz.idsc.amodeus.dispatcher.util.DistanceHeuristics;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.routing.CachedNetworkTimeDistance;
import ch.ethz.idsc.amodeus.routing.EasyMinDistPathCalculator;
import ch.ethz.idsc.amodeus.routing.EasyMinTimePathCalculator;
import ch.ethz.idsc.amodeus.routing.TimeDistanceProperty;
import ch.ethz.idsc.amodeus.util.geo.FastQuadTree;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVOperator;
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
 * - To limit computation time, a maximum length of the planned {@link SharedMenu} was introduced. */
public class TShareDispatcher extends SharedPartitionedDispatcher {

    /** general */
    private final int dispatchPeriod;
    private final TShareBipartiteMatchingUtils bipartiteMatchingUtils;
    private Tensor printInfo = Tensors.empty();

    /** T-Share specific */
    private final Map<VirtualNode<Link>, GridCell> gridCells = new HashMap<>();
    private final Scalar pickupDelayMax;
    private final Scalar drpoffDelayMax;
    private final DualSideSearch dualSideSearch;
    private final CachedNetworkTimeDistance distanceCashed;
    private final CachedNetworkTimeDistance travelTimeCalculator;

    protected TShareDispatcher(Network network, Config config, OperatorConfig operatorConfig, //
            TravelTime travelTime, AVRouter router, EventsManager eventsManager, //
            MatsimAmodeusDatabase db, VirtualNetwork<Link> virtualNetwork) {
        super(config, operatorConfig, travelTime, router, eventsManager, virtualNetwork, db);
        DispatcherConfigWrapper dispatcherConfig = DispatcherConfigWrapper.wrap(operatorConfig.getDispatcherConfig());
        dispatchPeriod = dispatcherConfig.getDispatchPeriod(30);
        DistanceHeuristics distanceHeuristics = dispatcherConfig.getDistanceHeuristics(DistanceHeuristics.EUCLIDEAN);
        System.out.println("Using DistanceHeuristics: " + distanceHeuristics.name());
        distanceCashed = new CachedNetworkTimeDistance(
                EasyMinDistPathCalculator.prepPathCalculator(network, new FastAStarLandmarksFactory(Runtime.getRuntime().availableProcessors())), //
                180000.0, TimeDistanceProperty.INSTANCE);
        travelTimeCalculator = new CachedNetworkTimeDistance(
                EasyMinTimePathCalculator.prepPathCalculator(network, new FastAStarLandmarksFactory(Runtime.getRuntime().availableProcessors())), //
                180000.0, TimeDistanceProperty.INSTANCE);
        bipartiteMatchingUtils = new TShareBipartiteMatchingUtils();

        /** T-Share specific */
        SafeConfig safeConfig = SafeConfig.wrap(operatorConfig.getDispatcherConfig());
        pickupDelayMax = Quantity.of(safeConfig.getInteger("pickupDelayMax", 10 * 60), SI.SECOND);
        drpoffDelayMax = Quantity.of(safeConfig.getInteger("drpoffDelayMax", 30 * 60), SI.SECOND);

        /** initialize grid with T-cells */
        QuadTree<Link> linkTree = FastQuadTree.of(network);
        for (VirtualNode<Link> virtualNode : virtualNetwork.getVirtualNodes()) {
            System.out.println("preparing grid cell: " + virtualNode.getIndex());
            gridCells.put(virtualNode, new GridCell(virtualNode, virtualNetwork, distanceCashed, travelTimeCalculator, linkTree));
        }
        dualSideSearch = new DualSideSearch(gridCells, virtualNetwork);
        System.out.println("According to the reference, a rectangular {@link VirtualNetwork} should be used.");
        System.out.println("Ensure that VirtualNetworkCreators.RECTANGULAR is used.");
    }

    @Override // TODO can I swith the order of single-use assignment and ride sharing assignment?
    protected void redispatch(double now) {
        final long round_now = Math.round(now);
        if (round_now % dispatchPeriod == 0) {

            /** STEP 1: search for options to to T-Share strategy ride sharing */
            doTShareRidesharing(now);

            /** STEP 2: unit capacity dispatching,in this implementation, global bipartite matching is used.
             * The following sets are used:
             * - vehicles: all divertable vehicles with zero passengers on board,
             * - requests: all requests not assigned to any vehicle */
            Collection<RoboTaxi> divertableAndEmpty = getDivertableRoboTaxis().stream()//
                    .filter(rt -> (rt.getUnmodifiableViewOfCourses().size() == 0)) //
                    .collect(Collectors.toList());
            printInfo = bipartiteMatchingUtils.executePickup(this, this::getCurrentPickupTaxi, divertableAndEmpty, //
                    getUnassignedAVRequests(), distanceCashed, now);
        }
    }

    private void doTShareRidesharing(double now) {
        /** update the roboTaxi planned locations */
        Collection<RoboTaxi> occupiedNotFull = getDivertableRoboTaxis().stream() //
                .filter(rt -> rt.getOnBoardPassengers() >= 1) // at least 1 passenger on board
                .filter(OnMenuRequests::canPickupAdditionalCustomer) // still capacity left
                .collect(Collectors.toList());

        Map<VirtualNode<Link>, Set<RoboTaxi>> plannedLocs = //
                RoboTaxiPlannedLocations.of(occupiedNotFull, virtualNetwork);

        /** do T-share ridesharing */
        List<AVRequest> sortedRequests = getAVRequests().stream() //
                .filter(avr -> !getCurrentPickupAssignements().keySet().contains(avr)) // requests are not scheduled to be picked up
                .sorted(RequestWaitTimeComparator.INSTANCE) // sort such that earliest submission is first
                .collect(Collectors.toList());

        for (AVRequest avr : sortedRequests) {

            // compute times left until pickup or dropoff window closed
            Scalar timeLeftForPickup = LatestPickup.timeTo(avr, pickupDelayMax, now);
            Scalar timeLeftUntilArrival = LatestArrival.timeTo(avr, drpoffDelayMax, travelTimeCalculator, now);

            /** if still available time, find ridesharing opportunity */
            if (Scalars.lessThan(Quantity.of(0, SI.SECOND), timeLeftForPickup) && //
                    Scalars.lessThan(Quantity.of(0, SI.SECOND), timeLeftForPickup)) {

                /** dual side search */
                Collection<RoboTaxi> potentialTaxis = dualSideSearch.apply(avr, plannedLocs, timeLeftForPickup, timeLeftUntilArrival);

                /** insertion feasibility check, compute possible insertions into schedules
                 * of all {@link RoboTaxi}s, find the insertion with smallest additional distance */
                NavigableMap<Scalar, InsertionChecker> insertions = new TreeMap<>();
                for (RoboTaxi taxi : potentialTaxis) {
                    InsertionChecker checker = //
                            new InsertionChecker(distanceCashed, travelTimeCalculator, taxi, avr, //
                                    pickupDelayMax, drpoffDelayMax, now);
                    if (Objects.nonNull(checker.getAddDistance()))
                        insertions.put(checker.getAddDistance(), checker);
                }

                /** plan update: insert the request into the plan of the {@link RoboTaxi} */
                if (Objects.nonNull(insertions.firstEntry()))
                    insertions.firstEntry().getValue().executeBest(this::addSharedRoboTaxiPickup);
            }
        }
    }

    @Override
    protected String getInfoLine() {
        return String.format("%s H=%s", //
                super.getInfoLine(), //
                printInfo.toString() /** This is where Dispatcher@ V... R... MR.. H is printed on console */
        );
    }

    public static class Factory implements AVDispatcherFactory {
        @Inject
        @Named(AVModule.AV_MODE)
        private TravelTime travelTime;

        @Inject
        private EventsManager eventsManager;

        @Inject
        private Config config;

        @Inject
        private MatsimAmodeusDatabase db;

        @Inject
        private Map<Id<AVOperator>, VirtualNetwork<Link>> virtualNetworks;

        @Override
        public AVDispatcher createDispatcher(OperatorConfig operatorConfig, AVRouter router, Network network) {
            return new TShareDispatcher(network, config, operatorConfig, travelTime, router, eventsManager, //
                    db, virtualNetworks.get(operatorConfig.getId()));
        }
    }
}
