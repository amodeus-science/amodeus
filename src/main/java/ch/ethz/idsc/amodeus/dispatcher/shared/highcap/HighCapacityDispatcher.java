/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.shared.highcap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.SharedRebalancingDispatcher;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseUtil;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.routing.EasyMinTimePathCalculator;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.router.AVRouter;

/** High-Capacity Algorithm from Alonso-Mora, Javier, et al. "On-demand high-capacity ride-sharing via dynamic trip-vehicle assignment."
 * Proceedings of the National Academy of Sciences 114.3 (2017): 462-467.
 * 
 * at each time dispatcher is called, for each vehicle, all possible trips (adding additional open request to the vehicle) is explored
 * and then ILP is called to choose the optimal assignment. */
public class HighCapacityDispatcher extends SharedRebalancingDispatcher {
    /** parameters */

    private static final double MAX_DELAY = 600.0;
    private static final double maxWaitTime = 300.0;
    private static final double costOfIgnoredReuqestNormal = 7200;
    private static final double costOfIgnoredReuqestHigh = 72000;
    private static final int DEFAULTNUMBERSEATS = 6;

    private final int dispatchPeriod;
    private final int rebalancePeriod;
    private final int capacityOfTaxi;

    private final List<Link> links;
    private final Random randGen = new Random(1234);
    private final double pickupDurationPerStop;
    private final double dropoffDurationPerStop;

    /** pool size (only the requests in this pool will be processed by algorithm) */
    private final int sizeLimit = 1000; // limit the size of valid open request
    /** RTV generator */
    private final AdvancedRTVGenerator rtvGG;
    private final double trafficTimeAllowance = 60;
    /** Path calculator */
    private final TravelTimeComputation ttc;
    /** RV generator */
    private final AdvanceTVRVGenerator rvGenerator;
    /** Cache */
    private final int sizeLimitOfCache = 200000;
    /** Loop Prevention */
    private final CheckingUpdateMenuOrNot checkingUpdateMenuOrNot;

    /** other objects that are needed in each dispatching/re-balance period */
    private List<TripWithVehicle> lastAssignment = new ArrayList<>(); // this is needed for latest pick up time modification
    private Set<AVRequest> requestMatchedLastStep = new HashSet<>();

    private Set<AVRequest> requestPool = new HashSet<>();
    private Set<AVRequest> lastRequestPool = new HashSet<>(); // this is used by RV and RTV generator to speed up the process
    private Set<AVRequest> overduedRequests = new HashSet<>();

    private Map<AVRequest, RequestKeyInfo> requestKeyInfoMap = new HashMap<>();

    public HighCapacityDispatcher(Network network, //
            Config config, OperatorConfig operatorConfig, //
            TravelTime travelTime, AVRouter router, EventsManager eventsManager, //
            MatsimAmodeusDatabase db) {

        super(config, operatorConfig, travelTime, router, eventsManager, db);
        SafeConfig safeConfig = SafeConfig.wrap(operatorConfig.getDispatcherConfig());
        dispatchPeriod = safeConfig.getInteger("dispatchPeriod", 30); // if want to change value, change in av file, here only for backup
        rebalancePeriod = safeConfig.getInteger("rebalancingPeriod", 60);// same as above
        capacityOfTaxi = (int) Long.parseLong(operatorConfig.getGeneratorConfig().getParams().getOrDefault("numberOfSeats", String.valueOf(DEFAULTNUMBERSEATS)));
        pickupDurationPerStop = safeConfig.getInteger("pickupDurationPerStop", 15);
        dropoffDurationPerStop = safeConfig.getInteger("dropoffDurationPerStop", 10);

        links = new ArrayList<>(network.getLinks().values());
        Collections.shuffle(links, randGen);

        FastAStarLandmarksFactory factory = new FastAStarLandmarksFactory();
        LeastCostPathCalculator lcpc = EasyMinTimePathCalculator.prepPathCalculator(network, factory);
        ttc = new TravelTimeComputation(lcpc, sizeLimitOfCache);
        rtvGG = new AdvancedRTVGenerator(capacityOfTaxi, pickupDurationPerStop, dropoffDurationPerStop);
        rvGenerator = new AdvanceTVRVGenerator(pickupDurationPerStop, dropoffDurationPerStop);
        checkingUpdateMenuOrNot = new CheckingUpdateMenuOrNot();
    }

    @Override
    protected void redispatch(double now) {
        final long round_now = Math.round(now);

        /** main part of the dispatcher */
        /** Construct RTV graph and use ILP to find optimal assignment */
        if (round_now % dispatchPeriod == 1) {
            System.out.println(now);
            // remove request that is no longer open in the map
            RequestTracker.removeClosedRequest(requestPool, getAVRequests());
            // remove request that deadline for pick up has passed
            overduedRequests = RequestTracker.removeOverduedRequest(requestPool, requestKeyInfoMap, now, requestMatchedLastStep);
            // put new open requests in requestKeyInfoMap (if size limit is not reached)
            for (AVRequest avRequest : getAVRequests()) {
                if (requestPool.size() < sizeLimit && !overduedRequests.contains(avRequest))
                    requestPool.add(avRequest);
                requestKeyInfoMap.computeIfAbsent(avRequest, avr -> new RequestKeyInfo(avr, maxWaitTime, MAX_DELAY, ttc));
            }
            // modify the request key info (submission time and pickup deadline)
            requestKeyInfoMap.forEach(((avRequest, requestKeyInfo) -> {
                requestKeyInfo.modifySubmissionTime(now, maxWaitTime, avRequest, overduedRequests); // see notes inside
                requestKeyInfo.modifyDeadlinePickUp(lastAssignment, avRequest, maxWaitTime); // according to paper
            }));

            Set<AVRequest> newAddedValidRequests = RequestTracker.getNewAddedValidRequests(requestPool, lastRequestPool); // write down new added requests
            Set<AVRequest> removedRequests = RequestTracker.getRemovedRequests(requestPool, lastRequestPool); // write down removed request
            Set<AVRequest> remainedRequests = RequestTracker.getRemainedRequests(requestPool, lastRequestPool); // write down remained request

            // remove the data from cache to release memory
            removedRequests.stream().filter(avRequest -> !overduedRequests.contains(avRequest)).map(AVRequest::getFromLink).forEach(ttc::removeEntry);

            // RV diagram construction
            Set<Set<AVRequest>> rvEdges = rvGenerator.generateRVGraph(newAddedValidRequests, removedRequests, remainedRequests, //
                    now, ttc, requestKeyInfoMap);

            // RTV diagram construction (generate a list of edges between trip and vehicle)
            List<TripWithVehicle> grossListOfRTVEdges = rtvGG.generateRTV(getDivertableRoboTaxis(), newAddedValidRequests, //
                    removedRequests, now, requestKeyInfoMap, //
                    rvEdges, ttc, lastAssignment, trafficTimeAllowance);

            // ILP
            // start
            List<TripWithVehicle> sharedTaxiAssignmentPlan = new ArrayList<>();
            if (!grossListOfRTVEdges.isEmpty()) {
                // we need to find the number of taxi in ILP
                List<RoboTaxi> listOfRoboTaxiWithValidTrip = grossListOfRTVEdges.stream().map(TripWithVehicle::getRoboTaxi).distinct().collect(Collectors.toList());

                List<AVRequest> validOpenRequestList = new ArrayList<>(requestPool);
                List<Double> iLPResultList = RunILP.of(grossListOfRTVEdges, validOpenRequestList, listOfRoboTaxiWithValidTrip, //
                        costOfIgnoredReuqestNormal, costOfIgnoredReuqestHigh, requestMatchedLastStep);
                for (int i = 0; i < grossListOfRTVEdges.size(); i++)
                    if (iLPResultList.get(i) == 1)
                        sharedTaxiAssignmentPlan.add(grossListOfRTVEdges.get(i));
            }
            // end

            // Taxi Assignment
            for (TripWithVehicle tripWithVehicle : sharedTaxiAssignmentPlan) {
                // get roboTaxi
                RoboTaxi roboTaxiToAssign = tripWithVehicle.getRoboTaxi();

                // get route (generated before)
                List<StopInRoute> routeToAssign = tripWithVehicle.getRoute();

                // assign
                List<SharedCourse> courseForThisTaxi = routeToAssign.stream() //
                        .map(StopInRoute::getSharedCourse) //
                        .collect(Collectors.toList());
                for (AVRequest avRequest : tripWithVehicle.getTrip())
                    addSharedRoboTaxiPickup(roboTaxiToAssign, avRequest);
                // create set of requests in the route
                Set<AVRequest> setOfAVRequestInRoute = routeToAssign.stream() //
                        .map(StopInRoute::getavRequest) //
                        .collect(Collectors.toSet());
                for (AVRequest avRequest : SharedCourseUtil.getUniqueAVRequests(roboTaxiToAssign.getUnmodifiableViewOfCourses()))
                    if (!setOfAVRequestInRoute.contains(avRequest))
                        abortAvRequest(avRequest);

                if (checkingUpdateMenuOrNot.updateMenuOrNot(roboTaxiToAssign, setOfAVRequestInRoute))
                    roboTaxiToAssign.updateMenu(courseForThisTaxi);
            }

            lastRequestPool.clear();
            lastRequestPool.addAll(requestPool);// stored to be used by next re-dispatch
            lastAssignment = sharedTaxiAssignmentPlan; // stored to be used by next re-dispatch
            requestMatchedLastStep.clear();
            sharedTaxiAssignmentPlan.stream().map(TripWithVehicle::getTrip).forEach(requestMatchedLastStep::addAll);
        }

        /** Re-balance */
        if (round_now % rebalancePeriod == 2) { // in order to avoid dispatch and re-balance happen at same time
            // check if there are both idling vehicles and unassigned requests at same time
            List<AVRequest> listOfUnassignedRequest = getUnassignedAVRequests();
            List<RoboTaxi> listOfIdlingTaxi = new ArrayList<>(getDivertableUnassignedRoboTaxis());

            // for (RoboTaxi roboTaxi : getRoboTaxis()) {
            // System.out.println("taxi id: " + roboTaxi.getId().toString() + ", link id:" + roboTaxi.getDivertableLocation().getId().toString() + //
            // "status: " + roboTaxi.getStatus() + ", Number of passenger: " + RoboTaxiUtils.getNumberOnBoardRequests(roboTaxi));
            // }

            if (!listOfIdlingTaxi.isEmpty() && !listOfUnassignedRequest.isEmpty()) {
                // re-balance
                // find optimal assignment of re-balance vehicle
                List<RebalanceTripWithVehicle> listOfAllRebalanceTripWithVehicle = new ArrayList<>();
                listOfAllRebalanceTripWithVehicle = RebalanceExplorer.of(listOfUnassignedRequest, listOfIdlingTaxi, //
                        now, ttc);
                List<RebalanceTripWithVehicle> rebalancePlan = RebalancePlanGenerator.of(listOfAllRebalanceTripWithVehicle);

                // assign Taxi to re-balance (first stop the taxi that is not in the new re-balance plan)
                Set<RoboTaxi> roboTaxisInNewRebalancePlan = rebalancePlan.stream().map(RebalanceTripWithVehicle::getRoboTaxi).collect(Collectors.toSet());
                for (RoboTaxi roboTaxi : listOfIdlingTaxi)
                    if (!roboTaxisInNewRebalancePlan.contains(roboTaxi))
                        setRoboTaxiRebalance(roboTaxi, roboTaxi.getDivertableLocation());
                for (RebalanceTripWithVehicle chosenRebalanceTask : rebalancePlan) {
                    RoboTaxi rebalanceRoboTaxi = chosenRebalanceTask.getRoboTaxi();
                    Link destinationOfRebalance = chosenRebalanceTask.getAvRequest().getFromLink();
                    setRoboTaxiRebalance(rebalanceRoboTaxi, destinationOfRebalance);
                }
            }
        }
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

        @Override
        public AVDispatcher createDispatcher(OperatorConfig operatorConfig, AVRouter router, Network network) {
            return new HighCapacityDispatcher(network, config, operatorConfig, travelTime, router, eventsManager, db);
        }
    }
}
