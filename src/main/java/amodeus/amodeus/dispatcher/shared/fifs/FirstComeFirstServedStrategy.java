/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodeus.dispatcher.shared.fifs;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.amodeus.components.AmodeusDispatcher;
import org.matsim.amodeus.components.AmodeusRouter;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.core.modal.ModalProviders.InstanceGetter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;

import amodeus.amodeus.dispatcher.core.RoboTaxi;
import amodeus.amodeus.dispatcher.core.RoboTaxiUsageType;
import amodeus.amodeus.dispatcher.core.RebalancingDispatcher;
import amodeus.amodeus.dispatcher.util.TreeMaintainer;
import amodeus.amodeus.dispatcher.util.TreeMultipleItems;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.net.TensorCoords;
import amodeus.amodeus.routing.CachedNetworkTimeDistance;
import amodeus.amodeus.routing.EasyMinTimePathCalculator;
import amodeus.amodeus.routing.TimeDistanceProperty;
import ch.ethz.idsc.tensor.Tensor;

/**
 * Implementation of the Algorithm presented in:
 * Fagnant, D. J., Kockelman, K. M., & Bansal, P. (2015). Operations of shared
 * autonomous vehicle fleet for
 * Austin, Texas, market. Transportation Research
 * Record: Journal of the Transportation Research Board, (2536), 98-106.
 */
public class FirstComeFirstServedStrategy extends RebalancingDispatcher {

    private final int dispatchPeriod = 300;

    /** ride sharing parameters */
    private static final int WAITLISTTIME = 300;
    private static final int MAXWAITTIME = 600; // Normal is 600
    private static final int EXTREEMWAITTIME = 3600 * 24; //
    private static final double BINSIZETRAVELDEMAND = 3600.0;
    private static final double REBALANCINGGRIDDISTANCE = 3218.69; // 2.0 miles in [m]
    private static final int MINNUMBERROBOTAXISINBLOCKTOREBALANCE = 5;
    private static final MaximumWaitTimeCalculator WAITTIME = new MaximumWaitTimeCalculator(MAXWAITTIME, WAITLISTTIME,
            EXTREEMWAITTIME);

    /** data structures are used to enable fast "contains" searching */
    private final TreeMaintainer<RoboTaxi> unassignedRoboTaxis;

    private final TreeMultipleItems<PassengerRequest> unassignedRequests;
    private final TreeMultipleItems<PassengerRequest> requestsLastHour;
    private final Set<PassengerRequest> waitList = new HashSet<>();
    private final Set<PassengerRequest> extremWaitList = new HashSet<>();

    private static final double MAXLAGTRAVELTIMECALCULATION = 1800.0;
    private final CachedNetworkTimeDistance timeDb;

    private final BlockRebalancing kockelmanRebalancing;

    protected FirstComeFirstServedStrategy(Network network, //
            Config config, AmodeusModeConfig operatorConfig, //
            TravelTime travelTime, AmodeusRouter router, EventsManager eventsManager, MatsimAmodeusDatabase db,
            RebalancingStrategy rebalancingStrategy) {
        super(config, operatorConfig, travelTime, router, eventsManager, db, rebalancingStrategy,
                RoboTaxiUsageType.SINGLEUSED);

        double[] networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        this.unassignedRoboTaxis = new TreeMaintainer<>(networkBounds, this::getRoboTaxiLoc);
        this.unassignedRequests = new TreeMultipleItems<>(PassengerRequest::getSubmissionTime);
        this.requestsLastHour = new TreeMultipleItems<>(PassengerRequest::getSubmissionTime);

        FastAStarLandmarksFactory factory = new FastAStarLandmarksFactory(Runtime.getRuntime().availableProcessors());
        LeastCostPathCalculator calculator = EasyMinTimePathCalculator.prepPathCalculator(network, factory);
        timeDb = new CachedNetworkTimeDistance(calculator, MAXLAGTRAVELTIMECALCULATION, TimeDistanceProperty.INSTANCE);

        this.kockelmanRebalancing = new BlockRebalancing(network, timeDb, MINNUMBERROBOTAXISINBLOCKTOREBALANCE,
                BINSIZETRAVELDEMAND, dispatchPeriod, REBALANCINGGRIDDISTANCE);
    }

    @Override
    protected void redispatch(double now) {
        final long round_now = Math.round(now);

        if (round_now % dispatchPeriod == 0) {
            /**
             * get open requests and available vehicles and put them into the desired
             * structures. Furthermore add all the requests to the one hour bin which is
             * used for rebalancing
             */

            /** prepare the registers for the dispatching */
            getDivertableUnassignedRoboTaxis().forEach(unassignedRoboTaxis::add);
            getUnassignedRequests().forEach(r -> {
                unassignedRequests.add(r);
                requestsLastHour.add(r);
            });
            requestsLastHour.removeAllElementsWithValueSmaller(now - BINSIZETRAVELDEMAND);

            /** calculate Rebalance before dispatching */
            Set<Link> lastHourRequests = requestsLastHour.getValues().stream().map(PassengerRequest::getFromLink)
                    .collect(Collectors.toSet());
            RebalancingDirectives rebalanceDirectives = kockelmanRebalancing.getRebalancingDirectives(round_now, //
                    lastHourRequests, //
                    unassignedRequests.getValues(), unassignedRoboTaxis.getValues());

            /**
             * for all {@link PassengerRequest}s in the order of their submission, try to
             * find the closest
             * vehicle and assign
             */
            Set<PassengerRequest> requestsToRemove = new HashSet<>();
            for (PassengerRequest avRequest : unassignedRequests.getTsInOrderOfValue()) {
                boolean assigned = false;
                if (unassignedRoboTaxis.size() > 0) {
                    RoboTaxi closestRoboTaxi = unassignedRoboTaxis.getClosest(getLocation(avRequest));
                    double travelTime = timeDb
                            .travelTime(closestRoboTaxi.getDivertableLocation(), avRequest.getFromLink(), now).number()
                            .doubleValue();
                    if (travelTime < WAITTIME.calculate(avRequest, waitList, extremWaitList)) {
                        setRoboTaxiPickup(closestRoboTaxi, avRequest, Double.NaN, Double.NaN);
                        unassignedRoboTaxis.remove(closestRoboTaxi);
                        rebalanceDirectives.removefromDirectives(closestRoboTaxi);
                        assigned = true;
                    }
                }
                /** If no {@link RoboTaxi} can be assigned, put the request on the wait list */
                if (assigned) {
                    requestsToRemove.add(avRequest);
                } else {
                    if (!waitList.add(avRequest))
                        extremWaitList.add(avRequest); // and if it was already on the wait list put it to the extreme
                                                       // wait list
                }
            }
            requestsToRemove.forEach(unassignedRequests::remove);

            /** execute New Rebalance Directives */
            rebalanceDirectives.getDirectives().forEach((rt, l) -> {
                setRoboTaxiRebalance(rt, l);
                unassignedRoboTaxis.remove(rt);
            });

            /**
             * For all robotaxis which were on rebalance and did not receive a new directive
             * stop on current link
             */
            getRebalancingRoboTaxis().stream() //
                    .filter(rt -> !rebalanceDirectives.getDirectives().containsKey(rt)) //
                    .forEach(rt -> setRoboTaxiRebalance(rt, rt.getDivertableLocation()));

        }
    }

    /**
     * @param request
     * @return {@link Coord} with {@link PassengerRequest} location
     */
    private static Tensor getLocation(PassengerRequest request) {
        return TensorCoords.toTensor(request.getFromLink().getFromNode().getCoord());
    }

    /**
     * @param roboTaxi
     * @return {@link Coord} with {@link RoboTaxi} location
     */
    private Tensor getRoboTaxiLoc(RoboTaxi roboTaxi) {
        return TensorCoords.toTensor(roboTaxi.getDivertableLocation().getCoord());
    }

    public static class Factory implements AVDispatcherFactory {
        @Override
        public AmodeusDispatcher createDispatcher(InstanceGetter inject) {
            Config config = (Config) inject.get(Config.class);
            MatsimAmodeusDatabase db = (MatsimAmodeusDatabase) inject.get(MatsimAmodeusDatabase.class);
            EventsManager eventsManager = (EventsManager) inject.get(EventsManager.class);

            AmodeusModeConfig operatorConfig = (AmodeusModeConfig) inject.getModal(AmodeusModeConfig.class);
            Network network = (Network) inject.getModal(Network.class);
            AmodeusRouter router = (AmodeusRouter) inject.getModal(AmodeusRouter.class);
            TravelTime travelTime = (TravelTime) inject.getModal(TravelTime.class);

            RebalancingStrategy rebalancingStrategy = (RebalancingStrategy) inject.getModal(RebalancingStrategy.class);

            return new FirstComeFirstServedStrategy(network, config, operatorConfig, travelTime, router, eventsManager,
                    db, rebalancingStrategy);
        }
    }
}
