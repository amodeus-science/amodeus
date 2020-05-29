/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.amodeus.components.AmodeusGenerator;
import org.matsim.amodeus.components.dispatcher.AVVehicleAssignmentEvent;
import org.matsim.amodeus.config.AmodeusModeConfig;
import org.matsim.amodeus.plpc.ParallelLeastCostPathCalculator;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.SimulationDistribution;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.SimulationObjectCompiler;
import ch.ethz.idsc.amodeus.net.SimulationObjects;
import ch.ethz.idsc.amodeus.net.StorageUtils;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/** This class contains all functionality which is used by both unit capacity
 * dispatchers and shared {@link RoboTaxi} dispatchers. */
/* package */ abstract class BasicUniversalDispatcher extends RoboTaxiMaintainer {

    protected int publishPeriod; // not final, so that dispatchers can disable, or manipulate

    final Set<PassengerRequest> pendingRequests = new LinkedHashSet<>();
    final MatsimAmodeusDatabase db;
    final FuturePathFactory futurePathFactory;
    protected final double pickupDurationPerStop;
    protected final double dropoffDurationPerStop;
    int total_matchedRequests = 0;
    private final String dispatcherMode;

    private Map<RoboTaxi, List<Link>> tempLocationTrace = new HashMap<>();

    public BasicUniversalDispatcher(EventsManager eventsManager, Config config, //
            AmodeusModeConfig operatorConfig, //
            TravelTime travelTime, ParallelLeastCostPathCalculator parallelLeastCostPathCalculator, //
            MatsimAmodeusDatabase db) {
        super(eventsManager, config, operatorConfig);
        this.db = db;
        futurePathFactory = new FuturePathFactory(parallelLeastCostPathCalculator, travelTime);
        pickupDurationPerStop = operatorConfig.getTimingConfig().getMinimumPickupDurationPerStop();
        dropoffDurationPerStop = operatorConfig.getTimingConfig().getMinimumDropoffDurationPerStop();
        SafeConfig safeConfig = SafeConfig.wrap(operatorConfig.getDispatcherConfig());
        publishPeriod = operatorConfig.getDispatcherConfig().getPublishPeriod();
        dispatcherMode = operatorConfig.getMode();
    }

    /** @return {@Collection} of all {@PassengerRequests} which are currently open.
     *         Requests are removed from list in setAcceptRequest function. */
    protected synchronized final Collection<PassengerRequest> getPassengerRequests() {
        return Collections.unmodifiableCollection(pendingRequests);
    }

    /** @return {@link List} of all {@link RoboTaxi}s which are in
     *         {@link RoboTaxiStatus} @param status, sample usage:
     *         getRoboTaxiSubset(AVStatus.STAY, AVStatus.DRIVEWITHCUSTOMER) */
    protected final List<RoboTaxi> getRoboTaxiSubset(RoboTaxiStatus... status) {
        return getRoboTaxiSubset(EnumSet.copyOf(Arrays.asList(status)));
    }

    protected final List<RoboTaxi> getRoboTaxiSubset(Set<RoboTaxiStatus> status) {
        return getRoboTaxis().stream() //
                .filter(rt -> status.contains(rt.getStatus())).collect(Collectors.toList());
    }

    /** @return {@Collection} of {@RoboTaxi}s which can be redirected during
     *         iteration, for a shared {@link RoboTaxi}, any vehicle can be diverted unless
     *         it has a directive in the current ime step or it is on the last link of its
     *         directive. */
    protected final Collection<RoboTaxi> getDivertableRoboTaxis() {
        return getRoboTaxis().stream() //
                .filter(RoboTaxi::isDivertable) //
                .collect(Collectors.toList());
    }

    /** Adding a @param vehicle during setup of simulation handled by {@link AmodeusGenerator},
     * the parameter @param singleOrShared indicates if multi-passenger ride-sharing case
     * or unit capacity case. */
    protected final void addVehicle(DvrpVehicle vehicle, RoboTaxiUsageType singleOrShared) {
        RoboTaxi roboTaxi = new RoboTaxi(vehicle, new LinkTimePair(vehicle.getStartLink(), 0.0), vehicle.getStartLink(), singleOrShared);
        Event event = new AVVehicleAssignmentEvent(dispatcherMode, vehicle.getId(), 0);
        addRoboTaxi(roboTaxi, event);
        tempLocationTrace.put(roboTaxi, new ArrayList<>());
    }

    /** called when a new request enters the system, adds request to
     * {@link #pendingRequests}, needs to be public because called from other not
     * derived MATSim functions which are located in another package */
    @Override
    public void onRequestSubmitted(PassengerRequest request) {
        boolean added = pendingRequests.add(request);
        GlobalAssert.that(added);
    }

    /** adds information to InfoLine */
    @Override
    protected String getInfoLine() {
        return String.format("%s R=(%5d) MR=%6d", //
                super.getInfoLine(), //
                getPassengerRequests().size(), //
                total_matchedRequests);
    }

    /** save simulation data into {@link SimulationObject} for later analysis and
     * visualization. */
    @Override
    protected final void notifySimulationSubscribers(long round_now, StorageUtils storageUtils) {
        if (publishPeriod > 0 && round_now % publishPeriod == 0 && round_now > 1) {
            SimulationObjectCompiler simulationObjectCompiler = SimulationObjectCompiler.create( //
                    round_now, getInfoLine(), total_matchedRequests, db);

            /** insert {@link RoboTaxi}s */
            simulationObjectCompiler.insertVehicles(tempLocationTrace);

            insertRequestInfo(simulationObjectCompiler);

            /** first pass vehicles typically empty, then no storage / communication of
             * {@link SimulationObject}s */
            SimulationObject simulationObject = simulationObjectCompiler.compile();
            if (SimulationObjects.hasVehicles(simulationObject))
                SimulationDistribution.of(simulationObject, storageUtils);

            /** the temporary location traces are flushed at this point as they have
             * been communicated, saved. */
            flushLocationTraces();
        }
    }

    /* package */ abstract void insertRequestInfo(SimulationObjectCompiler simulationObjectCompiler);

    @Override
    /* package */ void updateLocationTrace(RoboTaxi roboTaxi, Link lastLoc) {
        List<Link> trace = tempLocationTrace.get(roboTaxi);
        /** trace is empty or the position has changed */
        if (trace.isEmpty() || !lastLoc.equals(trace.get(trace.size() - 1)))
            trace.add(lastLoc);
    }

    private void flushLocationTraces() {
        tempLocationTrace.values().forEach(value -> {
            int size = value.size();
            if (size > 1)
                value.subList(0, size - 1).clear();
        });
    }
}