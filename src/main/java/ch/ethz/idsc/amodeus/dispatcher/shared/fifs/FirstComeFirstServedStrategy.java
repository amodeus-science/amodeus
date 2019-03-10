/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.RebalancingDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractRoboTaxiDestMatcher;
import ch.ethz.idsc.amodeus.dispatcher.util.AbstractVirtualNodeDest;
import ch.ethz.idsc.amodeus.dispatcher.util.EasyMinTimePathCalculator;
import ch.ethz.idsc.amodeus.dispatcher.util.EuclideanDistanceFunction;
import ch.ethz.idsc.amodeus.dispatcher.util.GlobalBipartiteMatching;
import ch.ethz.idsc.amodeus.dispatcher.util.RandomVirtualNodeDest;
import ch.ethz.idsc.amodeus.dispatcher.util.TreeMaintainer;
import ch.ethz.idsc.amodeus.dispatcher.util.TreeMultipleItems;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.matsim.av.config.AVDispatcherConfig;
import ch.ethz.matsim.av.config.AVGeneratorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;

/** Implementation of the Algorithm presented in:
 * Fagnant, D. J., Kockelman, K. M., & Bansal, P. (2015). Operations of shared autonomous vehicle fleet for austin, texas, market. Transportation Research
 * Record: Journal of the Transportation Research Board, (2536), 98-106. */
public class FirstComeFirstServedStrategy extends RebalancingDispatcher {

    private final int dispatchPeriod = 300;

    /** ride sharing parameters */
    private static final int WAITLISTTIME = 300;
    private static final int MAXWAITTIME = 600; // Normal is 600
    private static final int EXTREEMWAITTIME = 3600 * 24; //
    private static final double BINSIZETRAVELDEMAND = 3600.0;
    private static final double REBALANCINGGRIDDISTANCE = 3218.69; // 2.0 miles in [m]
    private static final int MINNUMBERROBOTAXISINBLOCKTOREBALANCE = 5;
    private static final MaximumWaitTimeCalculator WAITTIME = new MaximumWaitTimeCalculator(MAXWAITTIME, WAITLISTTIME, EXTREEMWAITTIME);

    /** data structures are used to enable fast "contains" searching */
    private final TreeMaintainer<RoboTaxi> unassignedRoboTaxis;

    private final TreeMultipleItems<AVRequest> unassignedRequests;
    private final TreeMultipleItems<AVRequest> requestsLastHour;
    private final Set<AVRequest> waitList = new HashSet<>();
    private final Set<AVRequest> extremWaitList = new HashSet<>();

    private static final double MAXLAGTRAVELTIMECALCULATION = 1800.0;
    private final TravelTimeComputationCached timeDb;

    private final BlockRebalancing kockelmanRebalancing;

    protected FirstComeFirstServedStrategy(Network network, //
            Config config, AVDispatcherConfig avDispatcherConfig, //
            TravelTime travelTime, AVRouter router, EventsManager eventsManager, MatsimAmodeusDatabase db) {
        super(config, avDispatcherConfig, travelTime, router, eventsManager, db);

        double[] networkBounds = NetworkUtils.getBoundingBox(network.getNodes().values());
        this.unassignedRoboTaxis = new TreeMaintainer<>(networkBounds, this::getRoboTaxiLoc);
        this.unassignedRequests = new TreeMultipleItems<>(this::getSubmissionTime);
        this.requestsLastHour = new TreeMultipleItems<>(this::getSubmissionTime);

        FastAStarLandmarksFactory factory = new FastAStarLandmarksFactory();
        LeastCostPathCalculator calculator = EasyMinTimePathCalculator.prepPathCalculator(network, factory);
        timeDb = TravelTimeComputationCached.of(calculator, MAXLAGTRAVELTIMECALCULATION);

        this.kockelmanRebalancing = new BlockRebalancing(network, timeDb, MINNUMBERROBOTAXISINBLOCKTOREBALANCE, BINSIZETRAVELDEMAND, dispatchPeriod, REBALANCINGGRIDDISTANCE);
    }

    @Override
    protected void redispatch(double now) {
        final long round_now = Math.round(now);

        if (round_now % dispatchPeriod == 0) {
            timeDb.update(now);

            /** get open requests and available vehicles and put them into the desired
             * structures. Furthermore add all the requests to the one hour bin which is
             * used for Rebalancing */

            /** prepare the registers for the dispatching */
            getDivertableUnassignedRoboTaxis().stream().forEach(rt -> unassignedRoboTaxis.add(rt));
            getUnassignedAVRequests().stream().forEach(r -> {
                unassignedRequests.add(r);
                requestsLastHour.add(r);
            });
            requestsLastHour.removeAllElementsWithValueSmaller(now - BINSIZETRAVELDEMAND);

            /** calculate Rebalance before dispatching */
            Set<Link> lastHourRequests = requestsLastHour.getValues().stream().map(avr -> avr.getFromLink()).collect(Collectors.toSet());
            RebalancingDirectives rebalanceDirectives = kockelmanRebalancing.getRebalancingDirectives(round_now, //
                    lastHourRequests, //
                    unassignedRequests.getValues(), unassignedRoboTaxis.getValues());

            /** for all AV Requests in the order of their submision, try to find the closest
             * vehicle and assign */
            Set<AVRequest> requestsToRemove = new HashSet<>();
            for (AVRequest avRequest : unassignedRequests.getTsInOrderOfValue()) {
                boolean assigned = false;
                if (unassignedRoboTaxis.size() > 0) {
                    RoboTaxi closestRoboTaxi = unassignedRoboTaxis.getClosest(getLocation(avRequest));
                    double travelTime = timeDb.timeFromTo(closestRoboTaxi.getDivertableLocation(), avRequest.getFromLink()).number().doubleValue();
                    if (travelTime < WAITTIME.calculate(avRequest, waitList, extremWaitList)) {
                        setRoboTaxiPickup(closestRoboTaxi, avRequest);
                        unassignedRoboTaxis.remove(closestRoboTaxi);
                        rebalanceDirectives.removefromDirectives(closestRoboTaxi);
                        assigned = true;
                    }
                }
                /** If we can not assign a robotaxi put the request on the wait list */
                if (assigned) {
                    requestsToRemove.add(avRequest);
                } else {
                    if (!waitList.contains(avRequest)) {
                        waitList.add(avRequest);
                    } else { // and if it was already on the wait list put it to the extrem wait list
                        extremWaitList.add(avRequest);
                    }
                }
            }
            requestsToRemove.forEach(avR -> unassignedRequests.remove(avR));

            /** execute New Rebalance Directives */
            rebalanceDirectives.getDirectives().forEach((rt, l) -> {
                setRoboTaxiRebalance(rt, l);
                unassignedRoboTaxis.remove(rt);
            });

            /** For all robotaxis which were on rebalance and did not receive a new directive
             * stop on current link */
            getRebalancingRoboTaxis().stream().//
                    filter(rt -> !rebalanceDirectives.getDirectives().containsKey(rt)).//
                    forEach(rt -> setRoboTaxiRebalance(rt, rt.getDivertableLocation()));

        }
    }

    /** @param request
     * @return {@link Double} with {@link AVRequest} submission Time */
    /* package */ Double getSubmissionTime(AVRequest request) {
        return request.getSubmissionTime();
    }

    /** @param request
     * @return {@link Coord} with {@link AVRequest} location */
    /* package */ Tensor getLocation(AVRequest request) {
        Coord coord = request.getFromLink().getFromNode().getCoord();
        return Tensors.vector(coord.getX(), coord.getY());
    }

    /** @param roboTaxi
     * @return {@link Coord} with {@link RoboTaxi} location */
    /* package */ Tensor getRoboTaxiLoc(RoboTaxi roboTaxi) {
        Coord coord = roboTaxi.getDivertableLocation().getCoord();
        return Tensors.vector(coord.getX(), coord.getY());
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

        @Override
        public AVDispatcher createDispatcher(AVDispatcherConfig avconfig, AVRouter router) {
            @SuppressWarnings("unused")
            AVGeneratorConfig generatorConfig = avconfig.getParent().getGeneratorConfig();

            @SuppressWarnings("unused")
            AbstractVirtualNodeDest abstractVirtualNodeDest = new RandomVirtualNodeDest();
            @SuppressWarnings("unused")
            AbstractRoboTaxiDestMatcher abstractVehicleDestMatcher = new GlobalBipartiteMatching(EuclideanDistanceFunction.INSTANCE);

            return new FirstComeFirstServedStrategy(network, config, avconfig, travelTime, router, eventsManager, db);
        }
    }
}
