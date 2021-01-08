/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.tshare;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.collections.QuadTree;

import com.google.inject.TypeLiteral;

import amodeus.amodeus.dispatcher.core.DispatcherConfigWrapper;
import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.RoboTaxiUsageType;
import amodeus.amodeus.dispatcher.core.PartitionedDispatcher;
import amodeus.amodeus.dispatcher.util.DistanceHeuristics;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.routing.CachedNetworkTimeDistance;
import amodeus.amodeus.routing.EasyMinDistPathCalculator;
import amodeus.amodeus.routing.EasyMinTimePathCalculator;
import amodeus.amodeus.routing.TimeDistanceProperty;
import amodeus.amodeus.util.geo.FastQuadTree;
import amodeus.amodeus.util.math.SI;
import amodeus.amodeus.util.matsim.SafeConfig;
import amodeus.amodeus.virtualnetwork.core.VirtualNetwork;
import amodeus.amodeus.virtualnetwork.core.VirtualNode;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;

/** Ma, Shuo, Yu Zheng, and Ouri Wolfson. "T-share: A large-scale dynamic taxi ridesharing service."
 * Data Engineering (ICDE), 2013 IEEE 29th International Conference on. IEEE, 2013.
 * 
 * Changes compared to the original version:
 * - The version presented in the publication considers only the addition of 1 trip to a trip which is
 * already being transported by a taxi. In order to operate the policy with taxis with capacity N, in this
 * version the time windows of all requests already in a taxi are checked before the insertion of a
 * new request is allowed.
 * - To limit computation time, a maximum length of the planned {@link SharedMenu} was introduced. */
public class TShareDispatcher extends PartitionedDispatcher {

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

    protected TShareDispatcher(Network network, Config config, AmodeusModeConfig operatorConfig, //
            TravelTime travelTime, AmodeusRouter router, EventsManager eventsManager, //
            MatsimAmodeusDatabase db, VirtualNetwork<Link> virtualNetwork, RebalancingStrategy rebalancingStrategy) {
        super(config, operatorConfig, travelTime, router, eventsManager, virtualNetwork, db, rebalancingStrategy, RoboTaxiUsageType.SHARED);
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

    @Override
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
                    getUnassignedRequests(), distanceCashed, now);
        }
    }

    private static boolean canPickupAdditionalCustomer(RoboTaxi robotaxi) {
        return robotaxi.getScheduleManager().getNumberOfOnBoardRequests() + 1 <= robotaxi.getCapacity();
    }

    private void doTShareRidesharing(double now) {
        /** update the roboTaxi planned locations */
        Collection<RoboTaxi> occupiedNotFull = getDivertableRoboTaxis().stream() //
                .filter(rt -> rt.getOnBoardPassengers() >= 1) // at least 1 passenger on board
                .filter(TShareDispatcher::canPickupAdditionalCustomer) // still capacity left
                .collect(Collectors.toList());

        Map<VirtualNode<Link>, Set<RoboTaxi>> plannedLocs = //
                RoboTaxiPlannedLocations.of(occupiedNotFull, virtualNetwork);

        /** do T-share ridesharing */
        List<PassengerRequest> sortedRequests = getPassengerRequests().stream() //
                .filter(avr -> !getCurrentPickupAssignements().keySet().contains(avr)) // requests are not scheduled to be picked up
                .sorted(RequestWaitTimeComparator.INSTANCE) // sort such that earliest submission is first
                .collect(Collectors.toList());

        for (PassengerRequest avr : sortedRequests) {

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
                    insertions.firstEntry().getValue().executeBest((rt, avreq) -> addSharedRoboTaxiPickup(rt, avreq, Double.NaN, Double.NaN));
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

            RebalancingStrategy rebalancingStrategy = inject.getModal(RebalancingStrategy.class);

            return new TShareDispatcher(network, config, operatorConfig, travelTime, router, eventsManager, //
                    db, virtualNetwork, rebalancingStrategy);
        }
    }
}
