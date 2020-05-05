/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.fifs;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.ModalProviders.InstanceGetter;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.dispatcher.core.DispatcherConfigWrapper;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedRebalancingDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.routing.CachedNetworkTimeDistance;
import ch.ethz.idsc.amodeus.routing.EasyMinTimePathCalculator;
import ch.ethz.idsc.amodeus.routing.TimeDistanceProperty;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;

/** Implementation of the ride sharing strategy used ind:
 * Fagnant, D. J., & Kockelman, K. M. (2015). Dynamic ride-sharing and optimal fleet sizing for a
 * system of shared autonomous vehicles (No. 15-1962).
 * 
 * The strategy goes through the Requests in the order of the submission time. For each request it
 * is first checked if a valid ride sharing possibility is present within a radius of 5 min (MAXWAITTIME).
 * If that is not the cse it is checked if another vehicle is available within 5 minutes which is currently
 * without designates requests. If this is also not possible then the vehicle is put on a wait list which
 * increases the search radius in the next dispatching step.
 * 
 * Ride Sharing is considered valid if 5 constraints are fulfilled:
 * 1. Current passengers’ trip duration increases ≤ 20% (total trip duration with ride-sharing vs. without ride-sharing);
 * 2. Current passengers’ remaining trip time increases ≤ 40%;
 * 3. New traveler’s total trip time increase grows by ≤ Max(20% total trip without ride-sharing, or 3 minutes);
 * 4. New travelers will be picked up at least within the next 5 minutes;
 * 5. Total planned trip time to serve all passengers ≤ remaining time to serve
 * the current trips + time to serve the new trip + drop-off time, if not pooled. */
public class DynamicRideSharingStrategy extends SharedRebalancingDispatcher {

    /** general Dispatcher Settings */
    private final int dispatchPeriod; // [s]

    /** unassigned Robo Taxis in the Scenario sorted by its coordinates in a Tree Structure */
    private final RoboTaxiHandler roboTaxiHandler;

    /** Normal: 300 [s], Time after which a request is put on to the wait list */
    private static final double WAITLISTTIME = 300.0;
    /** Normal is 600 [s] */
    private static final double MAXWAITTIME = 600.0;
    /** Unit: [s] The extreme wait list is used here as in AMoDeus requests should not be rejected.
     * This list guarantees for requests waiting for more than MaxWaitTime that a taxi can be found */
    private static final double EXTREEMWAITTIME = 3600.0 * 24;

    /** Maintains All the Information about the Requests. keeps track of Assignements, Pickups, ... */
    private final RequestHandler requestHandler = new RequestHandler(MAXWAITTIME, WAITLISTTIME, EXTREEMWAITTIME);

    /** Rebalancing Class to make use of a Grid Rebalancing. And its Parameters */
    private final BlockRebalancing rebalancing;
    private static final double BINSIZETRAVELDEMAND = 3600.0; // Assumption made for the Request records required for rebalancing
    private static final double REBALANCINGGRIDDISTANCE = 3218.69; // 2.0 miles in [m]
    private static final int MINNUMBERROBOTAXISINBLOCKTOREBALANCE = 5;

    /** Class which handles The Validation of routes. Afterwards The Constraints */
    private final RouteValidation routeValidation;
    private static final String MAXWAITTIMEID = "maxWaitTime";
    private static final String MAXDRIVETIMEINCREASEID = "maxDriveTimeIncrease";
    private static final String MAXREMAININGTIMEINCREASEID = "maxRemainingTimeIncrease";
    private static final String MAXABSOLUTETRAVELTIMEINCREASEID = "maxAbsolutDriveTimeIncrease";

    /** Travel Time Calculation */
    private final CachedNetworkTimeDistance timeDb;
    private static final double MAXLAGTRAVELTIMECALCULATION = 180000.0;

    protected DynamicRideSharingStrategy(Network network, //
            Config config, OperatorConfig operatorConfig, //
            TravelTime travelTime, AVRouter router, EventsManager eventsManager, //
            MatsimAmodeusDatabase db) {
        super(config, operatorConfig, travelTime, router, eventsManager, db);
        DispatcherConfigWrapper dispatcherConfig = DispatcherConfigWrapper.wrap(operatorConfig.getDispatcherConfig());
        dispatchPeriod = dispatcherConfig.getDispatchPeriod(300);

        SafeConfig safeConfig = SafeConfig.wrap(operatorConfig.getDispatcherConfig());
        double maxWaitTime = safeConfig.getInteger(MAXWAITTIMEID, 300); // normal is 300
        double maxDriveTimeIncrease = safeConfig.getDouble(MAXDRIVETIMEINCREASEID, 1.2); // normal is 1.2
        double maxRemainingTimeIncrease = safeConfig.getDouble(MAXREMAININGTIMEINCREASEID, 1.4); // normal is 1.4
        double newTravelTimeIncreaseAllowed = safeConfig.getInteger(MAXABSOLUTETRAVELTIMEINCREASEID, 180); // normal is 180 (=3min);

        roboTaxiHandler = new RoboTaxiHandler(network);

        FastAStarLandmarksFactory factory = new FastAStarLandmarksFactory(Runtime.getRuntime().availableProcessors());
        LeastCostPathCalculator calculator = EasyMinTimePathCalculator.prepPathCalculator(network, factory);
        timeDb = new CachedNetworkTimeDistance(calculator, MAXLAGTRAVELTIMECALCULATION, TimeDistanceProperty.INSTANCE);

        rebalancing = new BlockRebalancing(network, timeDb, MINNUMBERROBOTAXISINBLOCKTOREBALANCE, BINSIZETRAVELDEMAND, dispatchPeriod, REBALANCINGGRIDDISTANCE);

        routeValidation = new RouteValidation(maxWaitTime, maxDriveTimeIncrease, maxRemainingTimeIncrease, //
                dropoffDurationPerStop, pickupDurationPerStop, newTravelTimeIncreaseAllowed);
    }

    @Override
    protected void redispatch(double now) {
        final long round_now = Math.round(now);
        requestHandler.updatePickupTimes(getAVRequests(), now);

        if (round_now % dispatchPeriod == 0) {
            /** prepare the registers for the dispatching */
            roboTaxiHandler.update(getRoboTaxis(), getDivertableUnassignedRoboTaxis());
            requestHandler.addUnassignedRequests(getUnassignedAVRequests(), timeDb, now);
            requestHandler.updateLastHourRequests(now, BINSIZETRAVELDEMAND);

            /** calculate Rebalance before (!) dispatching */
            Set<Link> lastHourRequests = requestHandler.getRequestLinksLastHour();
            RebalancingDirectives rebalanceDirectives = rebalancing.getRebalancingDirectives(round_now, lastHourRequests, requestHandler.getCopyOfUnassignedAVRequests(),
                    roboTaxiHandler.getUnassignedRoboTaxis());

            /** for all AV Requests in the order of their submision, try to find the closest
             * vehicle and assign */
            for (AVRequest avRequest : requestHandler.getInOrderOffSubmissionTime()) {
                Set<RoboTaxi> robotaxisWithMenu = getRoboTaxis().stream()//
                        .filter(StaticHelper::plansPickupsOrDropoffs)//
                        .collect(Collectors.toSet());

                /** THIS IS WHERE WE CALCULATE THE SHARING POSSIBILITIES */
                Optional<Entry<RoboTaxi, List<SharedCourse>>> rideSharingRoboTaxi = routeValidation.getClosestValidSharingRoboTaxi(robotaxisWithMenu, avRequest, now, timeDb, //
                        requestHandler, roboTaxiHandler);

                if (rideSharingRoboTaxi.isPresent()) {
                    /** in Case we have a sharing possibility we assign */
                    RoboTaxi roboTaxi = rideSharingRoboTaxi.get().getKey();
                    GlobalAssert.that(routeValidation.menuFulfillsConstraints(roboTaxi, rideSharingRoboTaxi.get().getValue(), avRequest, now, timeDb, requestHandler));
                    addSharedRoboTaxiPickup(roboTaxi, avRequest);
                    requestHandler.removeFromUnasignedRequests(avRequest);
                    rebalanceDirectives.removefromDirectives(roboTaxi);
                    roboTaxi.updateMenu(rideSharingRoboTaxi.get().getValue());

                } else {
                    /** in Case No sharing possibility is present, try to find a close enough vehicle */
                    Optional<RoboTaxi> emptyRoboTaxi = RoboTaxiUtilsFagnant.getClosestUnassignedRoboTaxiWithinMaxTime(roboTaxiHandler, avRequest,
                            requestHandler.calculateWaitTime(avRequest), now, timeDb);
                    if (emptyRoboTaxi.isPresent()) {
                        /** In case we have a close vehicle which is free lets assign it */
                        addSharedRoboTaxiPickup(emptyRoboTaxi.get(), avRequest); // give directive
                        roboTaxiHandler.assign(emptyRoboTaxi.get()); // the assigned RoboTaxi is not unassigned anymore
                        rebalanceDirectives.removefromDirectives(emptyRoboTaxi.get()); // this taxi can not be rebalanced anymore
                        requestHandler.removeFromUnasignedRequests(avRequest); // the request is not unassigned anymore
                    } else {
                        /** Assignement was not possible as no Taxi was able to fulfill the constraints -> wait list! */
                        if (!requestHandler.isOnWaitList(avRequest))
                            requestHandler.addToWaitList(avRequest);
                        else // and if it was already on the wait list put it to the extreme wait list
                            requestHandler.addToExtreemWaitList(avRequest);
                    }
                }

            }

            /** execute New Rebalance Directives */
            for (Entry<RoboTaxi, Link> entry : rebalanceDirectives.getDirectives().entrySet()) {
                roboTaxiHandler.assign(entry.getKey());
                setRoboTaxiRebalance(entry.getKey(), entry.getValue());
            }

            /** For all robotaxis which were on rebalance and did not receive a new directive
             * stop on current link */
            getRebalancingRoboTaxis().stream() //
                    .filter(rt -> !rebalanceDirectives.getDirectives().containsKey(rt)) //
                    .forEach(rt -> setRoboTaxiRebalance(rt, rt.getDivertableLocation()));
            roboTaxiHandler.clear();
        }
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

            return new DynamicRideSharingStrategy(network, config, operatorConfig, travelTime, router, eventsManager, db);
        }
    }
}
